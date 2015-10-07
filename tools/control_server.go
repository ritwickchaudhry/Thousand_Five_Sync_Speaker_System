package main

import (
	"flag"
	"fmt"
	"io/ioutil"
	"net/http"
	"os"
	"strconv"
	"strings"
)

var listen string
var hostname string

func return_204(w http.ResponseWriter, r *http.Request) {
	w.WriteHeader(http.StatusNoContent)
}

func save_ids(w http.ResponseWriter, r *http.Request) {
	_ = "breakpoint"
	if r.Host != hostname {
		return
	}

	var err error

	if r.Method != "POST" {
		goto error
	}

	if err = r.ParseForm(); err != nil {
		goto error
	}

	if id, ok := r.PostForm["id"]; ok {
		if num, err := strconv.Atoi(id[0]); err != nil || num > 1024 {
			goto error
		}

		var addr string
		if addrs, ok := r.Header["X-Forwarded-For"]; ok {
			addr = addrs[len(addrs) - 1]
		} else {
			addr = strings.SplitN(r.RemoteAddr, ":", 2)[0]
		}
		addr = addr + " 4443"
		filename := id[0] + ".client"
		fmt.Fprintln(os.Stderr, "Saving", filename, "as", addr)
		ioutil.WriteFile(filename, []byte(addr), 0666)
		w.WriteHeader(http.StatusOK)
		return
	}

	error:
	w.WriteHeader(http.StatusMethodNotAllowed)
	return
}

func init() {
	flag.StringVar(&hostname, "hostname", "server.dns", "Hostname of the server to listen as")
	flag.StringVar(&listen, "listen", "0.0.0.0:3124", "Address to listen on")
	flag.Parse()
}

func main() {
	fmt.Fprintln(os.Stderr, "Binding to address", listen)
	http.HandleFunc("/generate_204", return_204)
	http.HandleFunc("/id", save_ids)
	http.ListenAndServe(listen, nil)
}

// vim:ft=go:noet:ts=4:sw=4
