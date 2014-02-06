(defproject rubyfuza "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [com.amazonaws/aws-java-sdk "1.6.12"]
                 [com.datomic/datomic-pro "0.9.4470"
                  :exclusions [org.slf4j/slf4j-nop
                               org.slf4j/slf4j-log4j12 com.amazonaws/aws-java-sdk
                               org.apache.httpcomponents/httpclient]]])
