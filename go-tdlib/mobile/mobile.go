package mobile

import (
	"encoding/json"

	"TGParser/parser"
)

type Parser struct{}

func (Parser) ParseChannel(url string) string {
	return marshalResult(parser.ParseWithInfo(url))
}

func (Parser) ParseChannelIncremental(url string, latestKnownMessageID int, pageLimit int) string {
	return marshalResult(parser.ParseWithInfoUntil(url, latestKnownMessageID, pageLimit))
}

func marshalResult(result *parser.ParseResult, err error) string {
	if err != nil {
		result = &parser.ParseResult{
			Error:    err.Error(),
			Channel:  parser.ChannelInfo{},
			Messages: []parser.Message{},
		}
	}

	output, err := json.MarshalIndent(result, "", "  ")
	if err != nil {
		return `{"error":"failed to encode json"}`
	}

	return string(output)
}
