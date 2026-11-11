package parser

import (
	"strings"
	"testing"

	"golang.org/x/net/html"
)

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
