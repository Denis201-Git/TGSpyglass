// Package parser scrapes public Telegram channel preview pages
// (e.g. https://t.me/s/<channel>) and extracts structured messages.
package parser

import (
	"errors"
	"fmt"
	"net/http"
	"regexp"
	"strconv"
	"strings"
	"time"
	"unicode"
	"unicode/utf16"

	"golang.org/x/net/html"
)

const (
	classMessageWrap     = "tgme_widget_message_wrap"
	classMessage         = "tgme_widget_message"
	classMessageText     = "tgme_widget_message_text"
	classMessageTextJS   = "js-message_text"
	classMessageReply    = "tgme_widget_message_reply"
	classServiceMessage  = "service_message"
	classServiceWidget   = "tgme_widget_message_service"
	classNoMessagesFound = "tme_no_messages_found"
	classMessageViews    = "tgme_widget_message_views"
	classMessageOwner    = "tgme_widget_message_owner_name"
	classMessageDate     = "tgme_widget_message_date"
	classMessagesMore    = "tme_messages_more"
	classPhotoWrap       = "tgme_widget_message_photo_wrap"
	classVideoPlayer     = "tgme_widget_message_video_player"
	classVideoThumb      = "tgme_widget_message_video_thumb"
	classVideoDuration   = "message_video_duration"
	classVideoTag        = "tgme_widget_message_video"
	classVideoBlocked    = "not_supported"
	classLinkPreview     = "tgme_widget_message_link_preview"
	classLinkImage       = "link_preview_image"
	classLinkTitle       = "link_preview_title"
	classLinkDesc        = "link_preview_description"
	classDocumentWrap    = "tgme_widget_message_document_wrap"
	classDocumentTitle   = "tgme_widget_message_document_title"
	classDocumentExtra   = "tgme_widget_message_document_extra"
	classChannelTitle    = "tgme_channel_info_header_title"
	classChannelUser     = "tgme_channel_info_header_username"

	attrBefore   = "data-before"
	attrHref     = "href"
	attrStyle    = "style"
	attrSrc      = "src"
	attrDatetime = "datetime"

	mediaTypePhoto       = "photo"
	mediaTypeVideo       = "video"
	mediaTypeLinkPreview = "link_preview"
	mediaTypeFile        = "file"

	userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36"

	maxPages = 1000
)

var ErrChannelNotFound = errors.New("channel not found")

var bgImageRegex = regexp.MustCompile(`background-image:url\(['"]?([^'")]+)['"]?\)`)

var httpClient = &http.Client{
	Timeout: 30 * time.Second,
}

func Parse(url string) ([]Message, error) {
	return parseMessages(url)
}

func ParseWithInfo(url string) (*ParseResult, error) {
	return parseResult(url)
}

func ParseWithInfoUntil(url string, latestKnownMessageID int, pageLimit int) (*ParseResult, error) {
	return parseResultUntil(url, latestKnownMessageID, pageLimit)
}

func parseResult(url string) (*ParseResult, error) {
	return parseResultUntil(url, 0, maxPages)
}

func parseResultUntil(url string, latestKnownMessageID int, pageLimit int) (*ParseResult, error) {
	if pageLimit <= 0 || pageLimit > maxPages {
		pageLimit = maxPages
	}

	result := &ParseResult{
		Messages: make([]Message, 0),
	}

	var before string

	seen := make(map[string]bool)

	for page := 0; page < pageLimit; page++ {

		pageURL := url
		if before != "" {
			pageURL = url + "?before=" + before
		}

		doc, err := fetchPage(pageURL)
		if err != nil {
			if page == 0 {
				return nil, err
			}

			// A failed deep-history page must not discard already parsed posts.
			break
		}

		if page == 0 {
			result.Channel = extractChannelInfo(doc)
		}

		messages := extractMessages(doc)

		if page == 0 && len(messages) == 0 {
			return nil, ErrChannelNotFound
		}

		for _, message := range messages {
			if seen[message.ID] {
				continue
			}

			seen[message.ID] = true
			result.Messages = append(result.Messages, message)
		}

		reachedKnown := containsKnownMessage(messages, latestKnownMessageID)

		next := getBeforeID(doc)
		if reachedKnown || next == "" || next == before {
			break
		}

		before = next
	}

	if result.Channel.ID == "" {
		if len(result.Messages) > 0 {
			result.Channel.ID = result.Messages[0].Channel
		}

		if result.Channel.ID == "" {
			result.Channel.ID = channelFromURL(url)
		}

		if result.Channel.Username == "" && result.Channel.ID != "" {
			result.Channel.Username = "@" + result.Channel.ID
		}
	}

	return result, nil
}

func containsKnownMessage(messages []Message, latestKnownMessageID int) bool {
	if latestKnownMessageID <= 0 {
		return false
	}

	for _, message := range messages {
		if message.MessageID <= latestKnownMessageID {
			return true
		}
	}

	return false
}

func channelFromURL(rawURL string) string {
	value := rawURL
	if index := strings.IndexAny(value, "?#"); index >= 0 {
		value = value[:index]
	}

	value = strings.TrimSuffix(value, "/")

	parts := strings.Split(value, "/")
	for index := len(parts) - 1; index >= 0; index-- {
		part := strings.TrimSpace(parts[index])
		if part == "" || part == "s" || strings.Contains(part, ".") || strings.Contains(part, ":") {
			continue
		}

		return part
	}

	return ""
}

func parseMessages(url string) ([]Message, error) {
	result, err := parseResult(url)
	if err != nil {
		return nil, err
	}

	return result.Messages, nil
}

func fetchPage(pageURL string) (*html.Node, error) {
	const attempts = 3

	var lastErr error

	for attempt := 0; attempt < attempts; attempt++ {
		if attempt > 0 {
			time.Sleep(time.Duration(attempt) * time.Second)
		}

		req, err := http.NewRequest("GET", pageURL, nil)
		if err != nil {
			return nil, fmt.Errorf("building request for %s: %w", pageURL, err)
		}
		req.Header.Set("User-Agent", userAgent)

		resp, err := httpClient.Do(req)
		if err != nil {
			lastErr = fmt.Errorf("getting %s: %w", pageURL, err)
			continue
		}

		if resp.StatusCode == http.StatusNotFound {
			resp.Body.Close()
			return nil, ErrChannelNotFound
		}

		if resp.StatusCode != http.StatusOK {
			lastErr = fmt.Errorf("getting %s: %s", pageURL, resp.Status)
			retry := resp.StatusCode == http.StatusTooManyRequests || resp.StatusCode >= 500
			resp.Body.Close()

			if retry {
				continue
			}

			return nil, lastErr
		}

		doc, err := html.Parse(resp.Body)
		resp.Body.Close()

		if err != nil {
			lastErr = fmt.Errorf("parsing %s as HTML: %w", pageURL, err)
			continue
		}

		return doc, nil
	}

	return nil, lastErr
}

func getBeforeID(n *html.Node) string {
	var finder func(*html.Node) string
	finder = func(n *html.Node) string {
		if n.Type == html.ElementNode && n.Data == "a" && hasClass(n, classMessagesMore) {
			return getAttr(n, attrBefore)
		}
		for c := n.FirstChild; c != nil; c = c.NextSibling {
			if id := finder(c); id != "" {
				return id
			}
		}
		return ""
	}
	return finder(n)
}

func extractMessages(n *html.Node) []Message {
	messages := make([]Message, 0)
	visitMessages(n, &messages)
	return messages
}

func visitMessages(n *html.Node, messages *[]Message) {
	if n.Type == html.ElementNode && n.Data == "div" && hasClass(n, classMessageWrap) {
		if !containsServiceMessage(n) {
			if msg := parseMessage(n); msg != nil {
				*messages = append(*messages, *msg)
			}
		}
	}

	for c := n.FirstChild; c != nil; c = c.NextSibling {
		visitMessages(c, messages)
	}
}

func containsServiceMessage(n *html.Node) bool {
	if n.Type == html.ElementNode {
		if hasClass(n, classServiceMessage) ||
			hasClass(n, classServiceWidget) ||
			hasClass(n, classNoMessagesFound) {
			return true
		}
	}

	for c := n.FirstChild; c != nil; c = c.NextSibling {
		if containsServiceMessage(c) {
			return true
		}
	}

	return false
}

func hasClass(n *html.Node, class string) bool {
	for _, a := range n.Attr {
		if a.Key == "class" {
			for _, c := range strings.Fields(a.Val) {
				if c == class {
					return true
				}
			}
		}
	}
	return false
}

func getAttr(n *html.Node, key string) string {
	for _, a := range n.Attr {
		if a.Key == key {
			return a.Val
		}
	}
	return ""
}

func findChild(n *html.Node, tag string, class string) *html.Node {
	for c := n.FirstChild; c != nil; c = c.NextSibling {
		if c.Type == html.ElementNode && c.Data == tag {
			if class == "" || hasClass(c, class) {
				return c
			}
		}
		if result := findChild(c, tag, class); result != nil {
			return result
		}
	}
	return nil
}

func findChildren(n *html.Node, tag string, class string) []*html.Node {
	var results []*html.Node
	for c := n.FirstChild; c != nil; c = c.NextSibling {
		if c.Type == html.ElementNode && c.Data == tag {
			if class == "" || hasClass(c, class) {
				results = append(results, c)
			}
		}
		results = append(results, findChildren(c, tag, class)...)
	}
	return results
}

func findMessageText(root *html.Node) *html.Node {
	if text := findChild(root, "div", classMessageTextJS); text != nil {
		return text
	}

	return findChildOutsideClass(root, "div", classMessageText, classMessageReply)
}

func findChildOutsideClass(n *html.Node, tag string, class string, excludedClass string) *html.Node {
	for c := n.FirstChild; c != nil; c = c.NextSibling {
		if c.Type == html.ElementNode && hasClass(c, excludedClass) {
			continue
		}

		if c.Type == html.ElementNode && c.Data == tag && hasClass(c, class) {
			return c
		}

		if result := findChildOutsideClass(c, tag, class, excludedClass); result != nil {
			return result
		}
	}

	return nil
}

func isInsideClass(n *html.Node, class string) bool {
	for parent := n.Parent; parent != nil; parent = parent.Parent {
		if parent.Type == html.ElementNode && hasClass(parent, class) {
			return true
		}
	}

	return false
}

func getText(n *html.Node) string {
	text, _ := getTextWithLinks(n)
	return text
}

func getTextWithLinks(n *html.Node) (string, []TextLink) {
	type draft struct {
		start int
		end   int
		url   string
	}

	var text strings.Builder

	drafts := make([]draft, 0)

	offset := 0

	var traverse func(*html.Node)
	traverse = func(node *html.Node) {
		if node.Type == html.TextNode {
			text.WriteString(node.Data)
			offset += utf16Length(node.Data)
			return
		}

		if node.Type == html.ElementNode && node.Data == "a" {
			href := strings.TrimSpace(getAttr(node, attrHref))
			if strings.HasPrefix(href, "//") {
				href = "https:" + href
			}

			start := offset

			for c := node.FirstChild; c != nil; c = c.NextSibling {
				traverse(c)
			}

			if isTextLinkHref(href) && offset > start {
				drafts = append(drafts, draft{
					start: start,
					end:   offset,
					url:   href,
				})
			}

			return
		}

		for c := node.FirstChild; c != nil; c = c.NextSibling {
			traverse(c)
		}
	}
	traverse(n)

	runes := []rune(text.String())

	left := 0
	for left < len(runes) && unicode.IsSpace(runes[left]) {
		left++
	}

	right := len(runes)
	for right > left && unicode.IsSpace(runes[right-1]) {
		right--
	}

	leftOffset := utf16RuneLength(runes[:left])
	length := utf16RuneLength(runes[left:right])

	links := make([]TextLink, 0, len(drafts))

	for _, item := range drafts {
		start := item.start - leftOffset
		end := item.end - leftOffset

		if start < 0 {
			start = 0
		}

		if end > length {
			end = length
		}

		if start >= end {
			continue
		}

		links = append(links, TextLink{
			Start:  start,
			Length: end - start,
			URL:    item.url,
		})
	}

	return string(runes[left:right]), links
}

func isTextLinkHref(href string) bool {
	lower := strings.ToLower(href)

	return strings.HasPrefix(lower, "http://") || strings.HasPrefix(lower, "https://")
}

func utf16Length(value string) int {
	length := 0

	for _, r := range value {
		length += utf16.RuneLen(r)
	}

	return length
}

func utf16RuneLength(runes []rune) int {
	length := 0

	for _, r := range runes {
		length += utf16.RuneLen(r)
	}

	return length
}

func parseMessage(wrap *html.Node) *Message {
	msgNode := findChild(wrap, "div", classMessage)
	if msgNode == nil {
		return nil
	}

	dataPost := getAttr(msgNode, "data-post")
	if dataPost == "" {
		return nil
	}

	parts := strings.Split(dataPost, "/")
	if len(parts) != 2 {
		return nil
	}
	channel := parts[0]
	messageID, _ := strconv.Atoi(parts[1])

	var text string

	var links []TextLink

	if textNode := findMessageText(msgNode); textNode != nil {
		text, links = getTextWithLinks(textNode)
	}

	var date time.Time
	if dateLink := findChild(msgNode, "a", classMessageDate); dateLink != nil {
		if timeNode := findChild(dateLink, "time", ""); timeNode != nil {
			if datetime := getAttr(timeNode, attrDatetime); datetime != "" {
				date, _ = time.Parse(time.RFC3339, datetime)
			}
		}
	}

	var views string
	if viewsNode := findChild(msgNode, "span", classMessageViews); viewsNode != nil {
		views = getText(viewsNode)
	}

	var author string
	if authorNode := findChild(msgNode, "a", classMessageOwner); authorNode != nil {
		author = getText(authorNode)
	}

	var url string
	if urlNode := findChild(msgNode, "a", classMessageDate); urlNode != nil {
		url = getAttr(urlNode, attrHref)
	}

	return &Message{
		ID:        dataPost,
		Channel:   channel,
		MessageID: messageID,
		Text:      text,
		Links:     links,
		Date:      date,
		Views:     views,
		Media:     extractMedia(msgNode),
		Author:    author,
		URL:       url,
	}
}

func extractChannelInfo(doc *html.Node) ChannelInfo {
	var info ChannelInfo

	if titleNode := findChild(doc, "div", classChannelTitle); titleNode != nil {
		info.Name = getText(titleNode)
	}

	if userNode := findChild(doc, "div", classChannelUser); userNode != nil {
		if link := findChild(userNode, "a", ""); link != nil {
			info.Username = getText(link)
			info.ID = strings.TrimPrefix(info.Username, "@")
		}
	}

	return info
}

func extractMedia(msgNode *html.Node) []Media {
	media := make([]Media, 0)

	for _, node := range findChildren(msgNode, "a", classPhotoWrap) {
		if isInsideClass(node, classMessageReply) {
			continue
		}

		media = append(media, Media{
			Type:     mediaTypePhoto,
			URL:      getAttr(node, attrHref),
			ThumbURL: extractBackgroundImage(getAttr(node, attrStyle)),
		})
	}

	for _, node := range findChildren(msgNode, "a", classVideoPlayer) {
		if isInsideClass(node, classMessageReply) {
			continue
		}

		thumbURL := ""
		if thumbNode := findChild(node, "i", classVideoThumb); thumbNode != nil {
			thumbURL = extractBackgroundImage(getAttr(thumbNode, attrStyle))
		}

		var duration string
		if durationNode := findChild(node, "time", classVideoDuration); durationNode != nil {
			duration = getText(durationNode)
		}

		videoURL := ""
		if videoTag := findChild(node, "video", classVideoTag); videoTag != nil {
			videoURL = getAttr(videoTag, attrSrc)
		}

		media = append(media, Media{
			Type:         mediaTypeVideo,
			URL:          videoURL,
			ThumbURL:     thumbURL,
			Duration:     duration,
			NotSupported: hasClass(node, classVideoBlocked),
		})
	}

	for _, node := range findChildren(msgNode, "a", classDocumentWrap) {
		if isInsideClass(node, classMessageReply) {
			continue
		}

		var title string
		if titleNode := findChild(node, "div", classDocumentTitle); titleNode != nil {
			title = getText(titleNode)
		}

		var desc string
		if descNode := findChild(node, "div", classDocumentExtra); descNode != nil {
			desc = getText(descNode)
		}

		media = append(media, Media{
			Type:  mediaTypeFile,
			URL:   getAttr(node, attrHref),
			Title: title,
			Desc:  desc,
		})
	}

	for _, node := range findChildren(msgNode, "a", classLinkPreview) {
		if isInsideClass(node, classMessageReply) {
			continue
		}

		thumbURL := ""
		if imgNode := findChild(node, "i", classLinkImage); imgNode != nil {
			thumbURL = extractBackgroundImage(getAttr(imgNode, attrStyle))
		}

		var title string
		if titleNode := findChild(node, "div", classLinkTitle); titleNode != nil {
			title = getText(titleNode)
		}

		var desc string
		if descNode := findChild(node, "div", classLinkDesc); descNode != nil {
			desc = getText(descNode)
		}

		media = append(media, Media{
			Type:     mediaTypeLinkPreview,
			URL:      getAttr(node, attrHref),
			ThumbURL: thumbURL,
			Title:    title,
			Desc:     desc,
		})
	}

	return media
}

func extractBackgroundImage(style string) string {
	matches := bgImageRegex.FindStringSubmatch(style)
	if len(matches) > 1 {
		return matches[1]
	}
	return ""
}
