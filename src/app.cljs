(ns app.core)

(def categories ["science" "tech" "politics" "philosophy" "business"])

(defn main []
  (let [root (sqeave/by-id "app")
        posts js/blogPosts]
    (sqeave/start-blog root posts categories)))

(main)
