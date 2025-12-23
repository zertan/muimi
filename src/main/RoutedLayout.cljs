
(defc RoutedLayout [this {:blog/keys [id posts post category]}]
  (let [navigate (useNavigate)
        params (useParams)]
    (createEffect
     (fn []
       (let [all (or (posts) [])
             pid (some-> (aget params "id") js/parseInt)
             target (or (some #(when (= pid (:post/id %)) %) all)
                        (first all))]
         (when target
           (select-post! this (id) target)
           (when (or (not pid) (not= pid (:post/id target)))
             (navigate (post-path target) {:replace true})))))))
  (let [[markdown _] (createResource (fn [] (:post/markdown (post))) fetch-markdown)
        [rendered _] (createResource markdown (fn [md]
                                                (when md
                                                  (-> (load-markdown-renderer)
                                                      (.then (fn [render] (render md)))))))]
    #jsx [:div {:class "layout"}
          [LeftPane {:toc (:toc (or (rendered) {}))}]
          [:main {:class "pane pane-main"}
           [Post {:blog/post (post)}]]
          [Aside {:ident [:blog/id (id)]}]]))
