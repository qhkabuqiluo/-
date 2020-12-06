package main

import (
	"flag"
	"fmt"
	"net/http"
	"sync"
	"time"
)

var m = make([]int, 0)

func main() {
	url := flag.String("url", "https://www.baidu.com", "url address")
	sum := flag.Int("s", 100, "sum request")
	concurrent := flag.Int("c", 10, "request concurrent")
	flag.Parse()
	total := 0
	var wg sync.WaitGroup
	fmt.Printf("url is %s, sum is %d, concurrent is %d \n", *url, *sum, *concurrent)
	for i := 0; i < *sum; {
		for j := 0; j < *concurrent; j++ {
			wg.Add(1)
			go fetch(*url, &wg)
			i++
		}

	}
	wg.Wait()
	for _, v := range m {
		total = total + v
	}
	average := total / *sum / 1000000
	average2 := float64(average) * 0.95
	fmt.Printf("Average response time is %d ms, 0.95Average response time is %.2f ms \n", average, average2)
}

func fetch(url string, wg *sync.WaitGroup) (string, error) {
	defer wg.Done()
	t1 := time.Now()
	resp, err := http.Get(url)
	t2 := time.Now()
	d := t2.Sub(t1)
	m = append(m, int(d))
	if err != nil {
		fmt.Println(err)
		return "", err
	}
	return resp.Status, nil
}
