(ns acme.frontend.app)

(defn init []
  (js/console.log
  (-> js/document
      (.getElementById "root")
      (.-innerHTML)
      (set! "Hello cljs & netlify"))))
