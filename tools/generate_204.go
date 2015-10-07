package main

import (
	"flag"
	"fmt"
	"net/http"
	"os"
)

var listen string

func return_204(w http.ResponseWriter, r *http.Request) {
	w.WriteHeader(http.StatusNoContent)
}

func init() {
	flag.StringVar(&listen, "listen", "0.0.0.0:3124", "Address to listen on")
	flag.Parse()
}

func main() {
	fmt.Fprintln(os.Stderr, "Binding to address ", listen)
	http.HandleFunc("/generate_204", return_204)
	http.ListenAndServe(listen, nil)
}

// vim:ft=go:noet:ts=4:sw=4
