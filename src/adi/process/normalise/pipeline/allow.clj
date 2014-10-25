(ns adi.process.normalise.pipeline.allow
  (:require [hara.common.checks :refer [hash-map?]]
            [hara.string.path :as path]
            [ribol.core :refer [raise]]))

(defn wrap-branch-model-allow
  "Works together with wrap-attr-model-allow to control access to data
  (normalise/normalise {:account/name \"Chris\"}
                       {:schema (schema/schema examples/account-name-age-sex)
                        :model {:allow {}}}
                       *wrappers*)
  => (raises-issue {:adi true
                    :data {:name \"Chris\"}
                    :key-path [:account]
                    :normalise true
                    :not-allowed true
                    :nsv [:account]})

  (normalise/normalise {:account/name \"Chris\"}
                       {:schema (schema/schema examples/account-name-age-sex)
                        :model {:allow {:account {:name :checked}}}}
                       *wrappers*)
  => {:account {:name \"Chris\"}}
  "
  {:added "0.3"}
  [f]
  (fn [subdata subsch nsv interim fns env]
    (let [suballow (:allow interim)
          nsubdata  (if (nil? suballow)
                        (raise [:adi :normalise :not-allowed
                                  {:data subdata :nsv nsv :key-path (:key-path interim)}]
                               (str "WRAP_BRANCH_MODEL_ALLOW: key " nsv " is not accessible."))
                        subdata)]
      (f nsubdata subsch nsv interim fns env))))

(defn wrap-attr-model-allow [f]
  (fn [subdata [attr] nsv interim fns env]
    (let [suballow (:allow interim)]
      (cond (= (:type attr) :ref)
            (cond (= suballow :yield)
                  (let [ynsv   (path/split (-> attr :ref :ns))
                        tmodel (get-in env (concat [:model :allow] ynsv))]
                    (f subdata [attr] ynsv (assoc interim :allow tmodel) fns env))

                  (or (= suballow :id) (hash-map? suballow))
                  (f subdata [attr] nsv interim fns env)

                  :else 
                  (raise [:adi :normalise :not-allowed
                            {:data subdata :nsv nsv :key-path (:key-path interim)}]
                         (str "WRAP_ATTR_MODEL_ALLOW: " nsv " is not accessible")))

            (or (nil? suballow) (not= suballow :checked))
            (let [nsubdata 
                  (raise [:adi :normalise :not-allowed
                            {:data subdata :nsv nsv :key-path (:key-path interim)}]
                         (str "WRAP_ATTR_MODEL_ALLOW: " nsv " is not accessible"))]
               (f nsubdata [attr] nsv interim fns env))

            :else
            (f subdata [attr] nsv interim fns env)))))
