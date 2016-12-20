(ns neon-snake.handlers
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [neon-snake.utils :as utils]
            [re-frame.core :refer [reg-event-db reg-sub dispatch]]
            [cljs.pprint :refer [pprint]]
            [goog.events :as events]))

(def board [20 20])

(def snake {:direction [0 1]
            :body [[8 2] [7 2] [6 2] [5 2]]})

(def initial-state {:board board
                    :snake snake
                    :point (utils/rand-free-position snake board)
                    :points 0
                    :game-running? true})

(reg-event-db
  :initialize
  (fn [db _]
    (utils/play-background-song)
    (merge db initial-state)))

(reg-event-db
  :next-state
  (fn
    [{:keys [snake board] :as db} _]
    (if (:game-running? db)
      (if (utils/collision? snake board)
        (do
          (update-in db [:game-running?] not)
          (utils/play-game-over-sound)
          (utils/pause-background-song))
        (-> db
            (update-in [:snake] utils/move-snake)
            (utils/process-move)))
      db)))

(reg-event-db
  :change-direction
  (fn [db [_ new-direction]]
    (update-in db [:snake :direction]
               (partial utils/change-snake-direction new-direction))))

; Global event listener for keydown event.
; Process key strokes according to 'utils/key-code->move' mapping

(defonce key-listener
         (events/listen js/window "keydown"
                        (fn [e]
                          (.log js/console "Key pressed")
                          (let [key-code (.-keyCode e)]
                            (when (contains? utils/key-code->move key-code)
                              (dispatch [:change-direction (utils/key-code->move key-code)]))))))

;; Subscriptions

(reg-sub
  :board
  (fn [db _]
    (:board db)))

(reg-sub
  :snake
  (fn [db _]
    (:body (:snake db))))


(reg-sub
  :point
  (fn [db _]
    (:point db)))

(reg-sub
  :points
  (fn [db _]
    (:points db)))

(reg-sub
  :game-running?
  (fn [db _]
    (:game-running? db)))
