(ns neon-snake.views
  (:require [re-frame.core :as reframe]
            [cljs.pprint :refer [pprint]]))


(defn render-board
  "Renders the game board area"
  []
  (let [board (reframe/subscribe [:board])
        snake (reframe/subscribe [:snake])
        point (reframe/subscribe [:point])]
    (fn []
      (let [[width height] @board
            snake-positions (into #{} @snake)
            current-point @point
            cells (for [y (range height)]
                    [:tr {:key y}
                          (for [x (range width)
                                :let [current-pos [x y]]]
                            (cond
                              (snake-positions current-pos) [:td.snake-on-cell {:key (str x y)}]
                              (= current-pos current-point) [:td.point {:key (str x y)}]
                              :else
                                [:td.cell {:key (str x y)}]))])]
        [:table.stage {:style {:height 377 :width 527}}
         [:tbody
          cells]]))))

(defn score
  "Renders player's score"
  []
  (let [points (reframe/subscribe [:points])]
    (fn []
      [:div
       [:div.game-title "Neon Snake" ]
       [:div.score (str "Score: " @points)]])))

(defn game-over
  "Renders the game over overlay"
  []
  (let [game-running (reframe/subscribe [:game-running?])]
    (fn []
      (if @game-running
        [:div]
        [:div.overlay
         [:div.play
          [:p "You killed Neon Snake, you bastard!"]
          [:h1 {:on-click #(reframe/dispatch [:initialize])} "↺"]]]))))

(defn game
  "The main rendering function"
  []
  [:div
   [score]
   [render-board]
   [game-over]])
