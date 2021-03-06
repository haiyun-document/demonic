(ns zolodeck.demonic.core-test
  (:use [clojure.test :only [run-tests deftest is are testing]]
        [zolodeck.demonic.core :only [init-db in-demarcation run-query delete] :as demonic]
        [zolodeck.demonic.helper :only [DATOMIC-TEST]]
        [zolodeck.demonic.test-schema]
        [zolodeck.demonic.test]))

(init-db "datomic:mem://demonic-test" TEST-SCHEMA-TX)

(defn cleanup-siva []
  (testing "cleanup of database"
    (demonic/in-demarcation
     (if-let [e-id (:db/id (find-by-fb-id (:id SIVA-FB)))]
       (demonic/delete e-id))
     (is (nil? (:db/id (find-by-fb-id (:id SIVA-FB))))))))

(deftest test-datomic-test-infra
  (testing "nothing exists to start"
    (demonic/in-demarcation   
     (is (nil? (:db/id (find-by-fb-id "10000"))))))

  (testing "within a test, you can CRUD correctly"
    (binding [DATOMIC-TEST true]
      (demonic/in-demarcation   
       (is (nil? (:db/id (find-by-fb-id "10000"))))
       (demonic/insert (assoc SIVA-DB :user/fb-id "10000"))
       (is (not (nil? (:db/id (find-by-fb-id "10000"))))))))

  (testing "after a test, DB is restored"
    (demonic/in-demarcation
     (is (nil? (:db/id (find-by-fb-id "10000")))))))


(deftest test-new-user-persistence
  (cleanup-siva)
  
  (testing "regular demarcations do persist at the end"
    (demonic/in-demarcation   
     (is (nil? (:db/id (find-by-fb-id (:id SIVA-FB)))))
     (demonic/insert SIVA-DB)
     (is (not (nil? (:db/id (find-by-fb-id (:id SIVA-FB))))))))

  (testing "regular demarcations are permanent"
    (demonic/in-demarcation   
     (is (not (nil? (:db/id (find-by-fb-id (:id SIVA-FB))))))))

  (cleanup-siva))

(demonictest demonictest-without-assertions
  (cleanup-siva)
  (demonic/insert SIVA-DB)
  (is (not (nil? (:db/id (find-by-fb-id (:id SIVA-FB)))))))

(deftest test-demonictest
  (testing "demonictests should not affect the db"
    (demonictest-without-assertions)
    (demonic/in-demarcation   
     (is (nil? (:db/id (find-by-fb-id (:id SIVA-FB))))))))

(deftest test-demonictesting
  (testing "demonictesting should not affect the db"
    (demonic/in-demarcation   
      (is (nil? (:db/id (find-by-fb-id (:id SIVA-FB))))))
    (demonic-testing "inserting siva, to later check that it wasnt really inserted"
      (demonic/insert SIVA-DB)
      (is (not (nil? (:db/id (find-by-fb-id (:id SIVA-FB)))))))
    (demonic/in-demarcation   
      (is (nil? (:db/id (find-by-fb-id (:id SIVA-FB))))))))

