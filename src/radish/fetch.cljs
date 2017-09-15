(ns radish.fetch)

(defn query-string
  [pairs]
  (if (empty? pairs)
    ""
    (reduce
      #(str % (if (> (count %) 1) "&") (first %2) "=" (js/encodeURIComponent (second %2)))
      "?"
      pairs)))

(defn parse-opts
  [opts]
  { "method" "GET"
    "url" (str
            (:url opts)
            (query-string (or (:query opts) [])))
    "headers" (or (:headers opts) {})})

(defn fetch!
  "Given an options map, callback, and errback, perform an HTTP request using the `fetch` API.
  Options (with defaults) are:
    - `:method \"GET\"` - The HTTP method to use
    - `:url \"\"` - A fully-qualified URL to request
    - `:query []` - A vector of key-value pairs to format as a querystring
    - `:headers {}` - A map of HTTP headers to include"
  [opts on-result]
  (let [parsed (parse-opts opts)
        fmt (or (:format opts) identity)
        promise (js/fetch (parsed "url") (clj->js parsed))]
    (-> promise
      (.then #(.json %))
      (.then #(on-result (fmt (js->clj %)))))))
