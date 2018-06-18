#FROM httpd:2.4
FROM centos/httpd-24-centos7
#EXPOSE 8080
#COPY ./public-html /usr/local/apache2/htdocs/
#COPY httpd.conf /usr/local/apache2/conf/httpd.conf

COPY ./public-html /opt/rh/httpd24/root/var/www/html
COPY httpd.conf /etc/httpd/conf

#USER root
#RUN mkdir /var/log/apache2
#RUN chmod -R 777 /var/log/apache2
