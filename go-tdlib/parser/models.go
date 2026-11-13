package parser

import "time"

type Message struct {
	ID        string    `json:"id"`
	Channel   string    `json:"channel"`
	MessageID int       `json:"message_id"`
	Text      string    `json:"text"`
	Date      time.Time `json:"date"`
	Views     string    `json:"views"`
	Media     []Media   `json:"media"`
	Author    string    `json:"author"`
	URL       string    `json:"url"`
}

type Media struct {
	Type         string `json:"type"`
	URL          string `json:"url"`
	ThumbURL     string `json:"thumb_url,omitempty"`
	Duration     string `json:"duration,omitempty"`
	Title        string `json:"title,omitempty"`
	Desc         string `json:"description,omitempty"`
	NotSupported bool   `json:"not_supported,omitempty"`
}

type ParseResult struct {
  Error    string      `json:"error,omitempty"`
  Channel  ChannelInfo `json:"channel"`
  Messages []Message   `json:"messages"`
}

type ChannelInfo struct {
	ID       string `json:"id"`
	Name     string `json:"name"`
	Username string `json:"username"`
}