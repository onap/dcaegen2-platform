# ---- Base Node ---- 
FROM node:12 AS base 

#RUN fs.inotify.max_user_watches=524288

WORKDIR /app 

#run in prod mode 
# RUN ng build --prod 

# copy project file 
COPY . . 

#to make healthcheck executable 
RUN chmod +x healthcheck.sh 

# COPY package.json /app 
RUN npm install -g @angular/cli@8.0.6 

#to install all dev dependencies explicitly 
RUN npm i --only=dev 
RUN npm install --save-dev @angular-devkit/build-angular 

# RUN npm run build --prod 
EXPOSE 4200 
CMD ng serve --host 0.0.0.0 --disableHostCheck --proxy-config proxy.conf.json
