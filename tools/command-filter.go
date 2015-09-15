package main

import (
    "encoding/binary"
    "bufio"
    "bytes"
    "fmt"
    "os"
)

var QUOTES = []byte("\"")

func main() {
    stdin := bufio.NewReader(os.Stdin);

    line, isPrefix, err := stdin.ReadLine()
    if err != nil {
        fmt.Fprintln(os.Stderr, "Couldn't read line", err)
        return
    }
    if isPrefix {
         fmt.Fprintln(os.Stderr, "Input line too long for Golang's default line buffers")
         return
    }

    line = bytes.Replace(line, QUOTES, []byte{}, -1)
    length := len(line)

    bs := make([]byte, 4);
    binary.BigEndian.PutUint32(bs, uint32(length))
    os.Stdout.Write(bs)
    // The routine above should ideally by just a `binary.Write` one-liner, but that doesn't work for unknown reasons

    binary.Write(os.Stdout, binary.BigEndian, line)
}
