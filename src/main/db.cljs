(ns db
  (:require ["../content/posts.js" :refer [posts categories] :rename {posts posts-data categories category-data}]))

(def categories
  (mapv (fn [cat]
          (keyword cat))
        category-data))

(defn- hydrate-post [p]
  {:post/id (.-id p)
   :post/slug (.-slug p)
   :post/title (.-title p)
   :post/summary (.-summary p)
   :post/category (keyword (.-category p))
   :post/tags (vec (js->clj (.-tags p)))
   :post/date (.-date p)
   :post/markdown (.-markdown p)})

(def posts
  (mapv hydrate-post posts-data))
