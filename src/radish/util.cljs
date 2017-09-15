(ns radish.util)

(defn filter-vals
  [f m]
  (reduce-kv (fn [acc k v] (if (f k v) (assoc acc k v) acc)) {} m))

(defn index-by
  [f coll]
  (reduce #(assoc % (f %2) %2) {} coll))