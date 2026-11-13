package parser

import (
	"io"
	"net/http"
	"net/http/httptest"
	"strings"
	"sync/atomic"
	"testing"

	"golang.org/x/net/html"
)

func TestExtractMessagesBigVideo(t *testing.T) {
	const page = `<div class="tgme_widget_message_wrap">
	  <div class="tgme_widget_message" data-post="durov/552">
	    <a class="tgme_widget_message_video_player not_supported js-message_video_player" href="https://t.me/durov/552">
	      <i class="tgme_widget_message_video_thumb" style="background-image:url('https://cdn.example/thumb.jpg')"></i>
	      <div class="tgme_widget_message_video_wrap"></div>
	      <div class="message_video_play"></div>
	      <time class="message_video_duration">1:15</time>
	      <div class="message_media_not_supported_wrap">
	        <div class="message_media_not_supported">
	          <div class="message_media_not_supported_label">Media is too big</div>
	          <span class="message_media_view_in_telegram">VIEW IN TELEGRAM</span>
	        </div>
	      </div>
	    </a>
	    <a class="tgme_widget_message_date" href="https://t.me/durov/552"><time datetime="2020-01-01T00:00:00Z"></time></a>
	  </div>
	</div>`
	doc, err := html.Parse(strings.NewReader(page))
	if err != nil {
		t.Fatal(err)
	}
	messages := extractMessages(doc)
	if len(messages) != 1 || len(messages[0].Media) != 1 {
		t.Fatalf("expected one message with one attachment, got %#v", messages)
	}
	want := (Media{Type: "video", ThumbURL: "https://cdn.example/thumb.jpg", Duration: "1:15", NotSupported: true})
	if got := messages[0].Media[0]; got != want {
		t.Errorf("attachment = %#v, want %#v", got, want)
	}
}

func TestExtractMessagesPlayableVideo(t *testing.T) {
	const page = `<div class="tgme_widget_message_wrap">
	  <div class="tgme_widget_message" data-post="telegram/441">
	    <a class="tgme_widget_message_video_player js-message_video_player" href="https://t.me/telegram/441">
	      <i class="tgme_widget_message_video_thumb" style="background-image:url('https://cdn.example/thumb.jpg')"></i>
	      <div class="tgme_widget_message_video_wrap"><video src="https://cdn.example/video.mp4" class="tgme_widget_message_video"></video></div>
	      <time class="message_video_duration">0:06</time>
	    </a>
	    <a class="tgme_widget_message_date" href="https://t.me/telegram/441"><time datetime="2020-01-01T00:00:00Z"></time></a>
	  </div>
	</div>`
	doc, err := html.Parse(strings.NewReader(page))
	if err != nil {
		t.Fatal(err)
	}
	messages := extractMessages(doc)
	if len(messages) != 1 || len(messages[0].Media) != 1 {
		t.Fatalf("expected one message with one attachment, got %#v", messages)
	}
	want := (Media{Type: "video", URL: "https://cdn.example/video.mp4", ThumbURL: "https://cdn.example/thumb.jpg", Duration: "0:06"})
	if got := messages[0].Media[0]; got != want {
		t.Errorf("attachment = %#v, want %#v", got, want)
	}
}

func TestExtractMessagesReplyUsesPostText(t *testing.T) {
	const page = `<div class="tgme_widget_message_wrap">
	  <div class="tgme_widget_message" data-post="durov/558">
	    <a class="tgme_widget_message_reply user-color-12" href="https://t.me/durov/557">
	      <div class="tgme_widget_message_author"><span class="tgme_widget_message_author_name">Pavel Durov</span></div>
	      <div class="tgme_widget_message_text js-message_reply_text">quoted comment text</div>
	    </a>
	    <div class="tgme_widget_message_text js-message_text">actual post text</div>
	    <a class="tgme_widget_message_photo_wrap" href="https://t.me/durov/558" style="background-image:url('https://cdn.example/post.jpg')"></a>
	    <a class="tgme_widget_message_date" href="https://t.me/durov/558"><time datetime="2020-01-01T00:00:00Z"></time></a>
	  </div>
	</div>`
	doc, err := html.Parse(strings.NewReader(page))
	if err != nil {
		t.Fatal(err)
	}
	messages := extractMessages(doc)
	if len(messages) != 1 {
		t.Fatalf("expected one message, got %#v", messages)
	}
	if messages[0].Text != "actual post text" {
		t.Errorf("text = %q, want actual post text", messages[0].Text)
	}
	if len(messages[0].Media) != 1 || messages[0].Media[0].URL != "https://t.me/durov/558" {
		t.Errorf("media = %#v, want only the post photo", messages[0].Media)
	}
}

func TestExtractMessagesSkipsServiceMessages(t *testing.T) {
	const page = `<div class="tgme_widget_message_wrap">
	  <div class="tgme_widget_message service_message" data-post="chat/1">
	    <div class="tgme_widget_message_text js-message_text">Channel created</div>
	  </div>
	</div>
	<div class="tgme_widget_message_wrap">
	  <div class="tgme_widget_message" data-post="chat/2">
	    <div class="tgme_widget_message_text js-message_text">Real post</div>
	  </div>
	</div>`
	doc, err := html.Parse(strings.NewReader(page))
	if err != nil {
		t.Fatal(err)
	}
	messages := extractMessages(doc)
	if len(messages) != 1 {
		t.Fatalf("expected one message, got %#v", messages)
	}
	if messages[0].Text != "Real post" {
		t.Errorf("text = %q, want Real post", messages[0].Text)
	}
}

func TestExtractMessagesDocument(t *testing.T) {
	const page = `<div class="tgme_widget_message_wrap">
	  <div class="tgme_widget_message" data-post="Ielts/39">
	    <a class="tgme_widget_message_document_wrap" href="https://t.me/Ielts/39">
	      <div class="tgme_widget_message_document_icon accent_bg"></div>
	      <div class="tgme_widget_message_document">
	        <div class="tgme_widget_message_document_title accent_color">example.pdf</div>
	        <div class="tgme_widget_message_document_extra">7 MB</div>
	      </div>
	    </a>
	    <a class="tgme_widget_message_date" href="https://t.me/Ielts/39"><time datetime="2020-01-01T00:00:00Z"></time></a>
	  </div>
	</div>`
	doc, err := html.Parse(strings.NewReader(page))
	if err != nil {
		t.Fatal(err)
	}
	messages := extractMessages(doc)
	if len(messages) != 1 || len(messages[0].Media) != 1 {
		t.Fatalf("expected one message with one attachment, got %#v", messages)
	}
	want := (Media{Type: "file", URL: "https://t.me/Ielts/39", Title: "example.pdf", Desc: "7 MB"})
	if got := messages[0].Media[0]; got != want {
		t.Errorf("attachment = %#v, want %#v", got, want)
	}
}

func TestParseResultUntilStopsAtKnownMessage(t *testing.T) {
	pages := map[string]string{
		"":   testPage("30", "29", "29"),
		"29": testPage("28", "27", "27"),
		"27": testPage("26", "25", ""),
	}

	var requests int32

	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		atomic.AddInt32(&requests, 1)
		page, ok := pages[r.URL.Query().Get("before")]
		if !ok {
			http.NotFound(w, r)
			return
		}
		io.WriteString(w, page)
	}))
	defer server.Close()

	oldClient := httpClient
	httpClient = server.Client()
	defer func() { httpClient = oldClient }()

	result, err := ParseWithInfoUntil(server.URL, 27, 0)
	if err != nil {
		t.Fatal(err)
	}
	if got := atomic.LoadInt32(&requests); got != 2 {
		t.Errorf("requests = %d, want 2", got)
	}
	if len(result.Messages) != 4 {
		t.Errorf("messages = %d, want 4", len(result.Messages))
	}
}

func TestParseResultUntilHonorsPageLimit(t *testing.T) {
	pages := map[string]string{
		"":   testPage("30", "29", "29"),
		"29": testPage("28", "27", "27"),
	}

	var requests int32

	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		atomic.AddInt32(&requests, 1)
		page, ok := pages[r.URL.Query().Get("before")]
		if !ok {
			http.NotFound(w, r)
			return
		}
		io.WriteString(w, page)
	}))
	defer server.Close()

	oldClient := httpClient
	httpClient = server.Client()
	defer func() { httpClient = oldClient }()

	result, err := ParseWithInfoUntil(server.URL, 0, 1)
	if err != nil {
		t.Fatal(err)
	}
	if got := atomic.LoadInt32(&requests); got != 1 {
		t.Errorf("requests = %d, want 1", got)
	}
	if len(result.Messages) != 2 {
		t.Errorf("messages = %d, want 2", len(result.Messages))
	}
}

func testPage(firstID string, secondID string, before string) string {
	more := ""
	if before != "" {
		more = `<a class="tme_messages_more" data-before="` + before + `"></a>`
	}

	return `<div class="tgme_widget_message_wrap">
	  <div class="tgme_widget_message" data-post="test/` + firstID + `">
	    <div class="tgme_widget_message_text js-message_text">first</div>
	  </div>
	</div>
	<div class="tgme_widget_message_wrap">
	  <div class="tgme_widget_message" data-post="test/` + secondID + `">
	    <div class="tgme_widget_message_text js-message_text">second</div>
	  </div>
	</div>` + more
}
