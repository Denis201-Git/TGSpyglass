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

	"golang.org/x/net/html"
)

const (
	classMessageWrap   = "tgme_widget_message_wrap"
	classMessage       = "tgme_widget_message"
	classMessageText   = "tgme_widget_message_text"
	classMessageViews  = "tgme_widget_message_views"
	classMessageOwner  = "tgme_widget_message_owner_name"
	classMessageDate   = "tgme_widget_message_date"
	classMessagesMore  = "tme_messages_more"
	classPhotoWrap     = "tgme_widget_message_photo_wrap"
	classVideoPlayer   = "tgme_widget_message_video_player"
	classVideoThumb    = "tgme_widget_message_video_thumb"
	classVideoDuration = "message_video_duration"
	classVideoTag      = "tgme_widget_message_video"
	classLinkPreview   = "tgme_widget_message_link_preview"
	classLinkImage     = "link_preview_image"
	classLinkTitle     = "link_preview_title"
	classLinkDesc      = "link_preview_description"
	classDocumentWrap  = "tgme_widget_message_document_wrap"
	classDocumentTitle = "tgme_widget_message_document_title"
	classDocumentExtra = "tgme_widget_message_document_extra"
	classChannelTitle  = "tgme_channel_info_header_title"
	classChannelUser   = "tgme_channel_info_header_username"

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

func parseResult(url string) (*ParseResult, error) {
	result := &ParseResult{
		Messages: make([]Message, 0),
	}

	var before string

	for page := 0; page < maxPages; page++ {

		pageURL := url
		if before != "" {
			pageURL = url + "?before=" + before
		}

		doc, err := fetchPage(pageURL)
		if err != nil {
			if errors.Is(err, ErrChannelNotFound) {
				return nil, ErrChannelNotFound
			}

			return nil, err
		}

		if page == 0 {
			result.Channel = extractChannelInfo(doc)
		}

		messages := extractMessages(doc)

		if page == 0 && len(messages) == 0 {
			return nil, ErrChannelNotFound
		}

		result.Messages = append(result.Messages, messages...)

		next := getBeforeID(doc)
		if next == "" || next == before {
			break
		}

		before = next
	}

	return result, nil
}

func parseMessages(url string) ([]Message, error) {
	result, err := parseResult(url)
	if err != nil {
		return nil, err
	}

	return result.Messages, nil
}

func fetchPage(pageURL string) (*html.Node, error) {
	req, err := http.NewRequest("GET", pageURL, nil)
	if err != nil {
		return nil, fmt.Errorf("building request for %s: %w", pageURL, err)
	}
	req.Header.Set("User-Agent", userAgent)

	resp, err := httpClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("getting %s: %w", pageURL, err)
	}
	defer resp.Body.Close()

	if resp.StatusCode == http.StatusNotFound {
		return nil, ErrChannelNotFound
	}

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("getting %s: %s", pageURL, resp.Status)
	}

	doc, err := html.Parse(resp.Body)
	if err != nil {
		return nil, fmt.Errorf("parsing %s as HTML: %w", pageURL, err)
	}
	return doc, nil
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
		if msg := parseMessage(n); msg != nil {
			*messages = append(*messages, *msg)
		}
	}

	for c := n.FirstChild; c != nil; c = c.NextSibling {
		visitMessages(c, messages)
	}
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

func getText(n *html.Node) string {
	var text strings.Builder
	var traverse func(*html.Node)
	traverse = func(n *html.Node) {
		if n.Type == html.TextNode {
			text.WriteString(n.Data)
		}
		for c := n.FirstChild; c != nil; c = c.NextSibling {
			traverse(c)
		}
	}
	traverse(n)
	return strings.TrimSpace(text.String())
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
	if textNode := findChild(msgNode, "div", classMessageText); textNode != nil {
		text = getText(textNode)
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
		media = append(media, Media{
			Type:     mediaTypePhoto,
			URL:      getAttr(node, attrHref),
			ThumbURL: extractBackgroundImage(getAttr(node, attrStyle)),
		})
	}

	for _, node := range findChildren(msgNode, "a", classVideoPlayer) {
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
			Type:     mediaTypeVideo,
			URL:      videoURL,
			ThumbURL: thumbURL,
			Duration: duration,
		})
	}

	for _, node := range findChildren(msgNode, "a", classDocumentWrap) {
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
