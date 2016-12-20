(ns neon-snake.core
  (:require [reagent.core :as reagent :refer [atom]]
            [re-frame.core :refer [dispatch dispatch-sync]]
            [neon-snake.views :refer [game]]
            [neon-snake.handlers :as handlers]))


(enable-console-print!)

(defonce snake-moving
         (js/setInterval #(dispatch [:next-state]) 80))


(defn run []
  (dispatch-sync [:initialize])
  (reagent/render [game]
                  (js/document.getElementById "app")))

(run)

(defn on-js-reload [])

