package mobile

import (
	"encoding/json"

	"TGParser/parser"
)

type Parser struct{}

func (Parser) ParseChannel(url string) string {

	result, err := parser.ParseWithInfo(url)
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