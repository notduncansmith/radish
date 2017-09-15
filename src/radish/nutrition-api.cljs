(ns radish.nutrition-api
  (:require
    [clojure.walk]
    [radish.fetch :refer [fetch!]]
    [radish.util :refer [filter-vals index-by]]
    [cljs.core.async :as a :refer [put! >!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

; https://ndb.nal.usda.gov/ndb/doc/index
(def URL_BASE "https://api.nal.usda.gov/ndb")
(def API_KEY "U64AdSe3XOKwuKw6dDvyZwKBeHH7l9MeocKqr6FO")
(defn api-path [p] (str URL_BASE p))

(def food-search-requests (a/chan 100))
(def food-report-requests (a/chan 100))

(def food-searches (a/chan 100))
(def food-reports (a/chan 100))

; https://ndb.nal.usda.gov/ndb/doc/apilist/API-SEARCH.md
(defn- search-request
  [query]
  { :url (api-path "/search/")
    :format #(% "list")
    :query
      [ ["format" "json"]
        ["api_key" API_KEY]
        ["max" 100]
        ["ds" "Standard Reference"]
        ["q" query]]})

; https://ndb.nal.usda.gov/ndb/doc/apilist/API-FOOD-REPORTV2.md - note "V2", there's a similar-looking "V1"
(defn- food-details-request
  [ids]
  { :url (api-path "/V2/reports") ; note: flipped from documented path ("/reports/V2")
    :format (fn [response] (mapv #(% "food") (response "foods")))
    :query
      (into (mapv #(conj ["ndbno"] %) (take 50 ids))
        [ ["format" "json"]
          ["api_key" API_KEY]
          ["type" "b"]])})

(defn- format-nutrient
  [nutrient]
  (-> nutrient
    (clojure.walk/keywordize-keys)
    (update :value #(js/parseFloat % 10))))
     ; TODO - use this in other formatting functions

(defn- format-food-report
  [item]
  (let [desc (item "desc")]
    { :id (desc "ndbno")
      :ndbno (desc "ndbno")
      :name (desc "name")
      :group (desc "fg")
      :nutrients
        (index-by
          :name
          (mapv format-nutrient (item "nutrients")))}))

(defn- format-food-search-result
  [item]
  { :id (item "ndbno")
    :ndbno (item "ndbno")
    :name (item "name")
    :group (item "group")})

(defn fetch-food-report!
  [food-ids]
  (do
    (println "Fetching food ids" food-ids)
    (fetch! (food-details-request food-ids)
      (fn [response]
        (go (>! food-reports (index-by :id (map format-food-report response))))))))

(defn fetch-food-search!
  [q]
  (do
    (println "Searching for query " q)
    (fetch! (search-request q)
      (fn [response]
        (let [results (mapv format-food-search-result (response "item"))]
          (go (>! food-searches {q results})))))))

