(ns radish.core
  (:require
    [cljs.core.async :as a :refer [put! >! <!]]
    [clojure.core.match :as m :refer [match]]
    [reagent.core :as r]
    [radish.ui :as ui]
    [radish.nutrition-api :as api]
    [radish.requirements :as nutritional-requirements])
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]]))

(enable-console-print!)

(defonce app-state
  (r/atom
    { :foods {}
      :groups {} ; TODO - Hardcode group info and load it here
      :requirements nutritional-requirements/for-duncan
      :basket []
      :dom-nodes {}
      :search { :current-term "kale"
                :currently-searching false
                :results {}}
      :explore {}
      :detail-panels {}}))

(def basket-actions (a/chan 100))

(defn set-in! [ks v] (do (println "Setting in" ks v) (swap! app-state assoc-in ks v)))
(defn swap-in! [ks f] (do (println "Swapping in" ks f) (swap! app-state update-in ks f)))

(defn nq! [c v]
  (do
    (println "Enqueueing " v " on " c)
    (go (put! c v))))

(defn close-details-panel! [id] (swap-in! [:detail-panels] #(dissoc % id)))

(defn cache-enabled
  [check-cache perform-action]
  (let [cached (check-cache)]
    (if-not (nil? cached) (println "Got cached" cached) (perform-action))))

(defn cache-enabled-food-search!
  [query]
  (cache-enabled
    #(get-in @app-state [:search :results query])
    #(nq! api/food-search-requests query)))

(defn cache-enabled-food-report!
  [food-ids]
  ; TODO - factor out lookups and only nq missing ones
  (cache-enabled
    #(if (every? (partial contains? (:foods @app-state)) food-ids) (select-keys (:foods @app-state) food-ids) nil)
    #(nq! api/food-report-requests food-ids)))

(defn open-details-panel! [item-id]
  (do
    (println "Opening detail panel for" item-id)
    (cache-enabled-food-report! [item-id])
    (set-in! [:detail-panels item-id :id] item-id)))

(defn add-basket-item!
  [food-id measure]
  (swap-in!
    [:basket]
    #(conj %
      { :id food-id
        :measure measure
        :remove! (fn [] (nq! basket-actions [:remove food-id measure]))})))

(defn process-basket-action!
  [action]
  (match action
    [:add {:id id :measure m}] (add-basket-item! id m)))


(defn start-event-loop! []
  (go-loop [prev nil]
    (print "Processed: " prev)
    (recur
      (alt!
        basket-actions ([basket-action] (process-basket-action! basket-action))
        api/food-report-requests ([food-ids] (api/fetch-food-report! food-ids))
        api/food-reports ([report] (swap-in! [:foods] #(merge-with merge % report)))
        api/food-search-requests ([query] (api/fetch-food-search! query))
        api/food-searches
          ( [resultset]
            (do
              (println "Food search results" resultset)
              (set-in! [:search :results] resultset)))))))

(defn app []
  (let [state @app-state
        {:keys [foods basket search explore detail-panels requirements]} state]
    [ui/layout state
      [:div.pure-g
        [:div.pure-u-1-5
          [:h2.heading "Basket"]
          [ui/basket basket]]
        [:div.pure-u-2-5
          [:h2.heading "Search / Browse"]
          [ui/search-bar (merge search {:on-term-change (partial set-in! [:search :current-term])
                                        :on-active-toggle (partial set-in! [:search :currently-searching])
                                        :on-search cache-enabled-food-search!
                                        :on-select (fn [item-id] (open-details-panel! item-id))})]
          [:h2.heading "Explore"]
          [ui/food-explorer explore foods]]]
      (into [:div.detail-panels]
        (mapv
          #(into
            [ui/detail-panel]
            [ (foods (:id %))
              requirements
              (fn [] (close-details-panel! (:id %)))])
          (vals detail-panels)))]))

(defn on-js-reload [])

(r/render
  [app]
  (js/document.getElementById "app"))

(start-event-loop!)






























