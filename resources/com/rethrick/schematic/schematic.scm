
(define body
  (lambda (response data)
    (.setBody response data)))

(define status
  (lambda (response code)
    (.setStatus response code)))

(define after
  (lambda (response data)
    (.setCallback response data)))

; template support
(define template:mvel
  (lambda (response rec) (body response
    (com.rethrick.schematic.Tools.template
     (string-append --file-- ".mvel") rec))))

(define put-java-map
  (lambda (j-map ls)
    (if (null? ls)
      j-map
      (begin
        (.put j-map (car ls) (cadr ls))
        (put-java-map j-map (cddr ls))))))

(define record
  (lambda ls
    (display ls)))
    ;(put-java-map (java.util.HashMap.) ls)))


; request discovery
(define request/uri
  (lambda (request) (.getUri request)))

(define request/method
  (lambda (request) (.toLowerCase (.toString (.getMethod request)))))

(define request/user-agent
  (lambda (request) (.getHeader request "User-Agent")))

(define request/content-type
  (lambda (request) (.getHeader request "Content-Type")))

(define request/content-length
  (lambda (request) (.getHeader request "Content-Length")))

(define request/ip
  (lambda (request) (.getHeader request "Content-Length")))

