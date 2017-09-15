(ns radish.ui
  (:require
    [cljsjs.chartjs]
    [cljs.core.async :as a :refer [chan >!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def events (chan 1000))
(defn publish! [type value] (go (>! events {:type type :value value})))

(defn layout
  [state & content]
  [:div
    [:h1.text-logo "Radish " [:span.tagline "Super nutrition for regular people"]]
    [:p.data-view {:on-click #(js/alert (pr-str state))} "Data View"]
    (into [:div.content {}] content)])

(defn search-result
  [item on-select]
  [:a.item {:href "#"
            :on-click #(do (println "Clicked " (:name item)) (.preventDefault %) (on-select (:id item)))}
    (:name item)])

(defn search-results
  [results current-term on-select currently-searching]
  (let [class-name (str "results " (if currently-searching "active" "inactive"))
        possible-result-sets (map #(results %) (filter #(= 0 (.indexOf % current-term)) (keys results)))
        result-list (reduce into [] possible-result-sets)]
    [:div {:class-name class-name}
      (into [:div.result-wrapper]
        (mapv #(into [] [search-result % on-select]) result-list))]))

(defn search-bar
  [{:keys [current-term currently-searching results on-term-change on-select on-search on-active-toggle]}]
  [:div.search-bar
    [:form {:on-submit #(do (.preventDefault %) (on-search current-term))}
      [:input { :type "text"
                :value current-term
                :on-change (fn [e] (on-term-change (.. e -target -value)))
                :on-focus (fn [e] (on-active-toggle true))
                :on-blur (fn [e] (do (js/setTimeout #(on-active-toggle false) 100)) false)}]]
    [search-results results current-term on-select currently-searching]])

(defn nutrient-percentage
  [nutrient requirements]
  (let [[base-name rest-name] (.split (:name nutrient) ",")
        requirement (requirements base-name)
        nval (:value nutrient)
        rval (:value requirement)
        result (println base-name nval rval)
        units-match (= (:unit nutrient) (:unit requirement))]
    (if (and (> nval 0) (not= nil rval) units-match)
      (str "(" (js/window.toFixed (* 100.0 (/ nval rval)) 2) "%)")
      "")))

(defn nutrient-table
  [nutrients requirements]
  [:div.table-wrapper>table.nutrient-table.pure-table
    (->> nutrients
      (reduce-kv #(conj % [:tr [:td %2] [:td (str (:value %3) (:unit %3) " " (nutrient-percentage %3 requirements))]]) [])
      (sort-by #(-> % (second) (second)))
      (into [:tbody]))])

(defn detail-panel
  [item requirements on-close]
  [:div.detail-panel {:style {"backgroundColor" (:color item)}}
    [:a.close {:href "#" :on-click #(do (.preventDefault %) (on-close))} "⨯"]
    [:h3.title (:name item)]
    [nutrient-table (:nutrients item) requirements]])

(defn basket-item
  [item]
  [:div.basket-item {}
    [:p.name (:name item)]])

(defn basket
  [items]
  (into [:div.basket] (mapv basket-item items)))

(defn food-explorer
  [explorer-state food-data update!]
  [:pre "<explorer goes here>"])

; (def radar-chart
;   (r/create-class
;     {:get-initial-state (fn [this])
;      :component-will-receive-props (fn [this new-argv])
;      :should-component-update (fn [this old-argv new-argv])
;      :component-did-mount (fn [this])
;      :component-will-update (fn [this new-argv])
;      :component-did-update (fn [this old-argv])
;      :component-will-unmount (fn [this])
;      :reagent-render (fn [args])}))   ;; or :render (fn [this])

; (defn item-chart
;   [item]
;   [radar-chart
;     { :axes [{:name "kcal"} {:name "fat (g)"} {:name "weight (g)"}]
;       :data [{:name (:name item) :values (:nutritional-values item)}]}])


























