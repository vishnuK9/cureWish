FROM ubuntu

RUN apt-get update

RUN apt-get install apache2 -y

# RUN apt-get install git -y
# RUN apt install apache2-utils -y

RUN apt clean

# RUN git clone https://github.com/vishnuK9/cureWish.git
# RUN cp -r cureWish/* /var/www/html/

COPY images index.html /var/www/html/

EXPOSE 80

CMD apache2ctl -D FOREGROUND
