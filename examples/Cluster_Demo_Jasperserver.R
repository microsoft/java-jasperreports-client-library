library(cluster)

#uncomment these lines to test in standalone mode
#dataset <- "mtcars"
#clusters <- 4
#clustering_type <- 'hclust'
#plotwidth<-555
#plotheight<-555


if (dataset == 'iris') {
  data <- iris[,1:4]
  df <-data.frame(data$Sepal.Length, data$Sepal.Width)

} else if (dataset == 'mtcars') {
  data <- mtcars
  df <-data.frame(data$drat, data$disp)
} else {
  stop("Enter a valid dataset")
}

if (clustering_type == 'kmeans') {

  fit <- kmeans(df, clusters)
  png("cluster_output.png", width=plotwidth, height=plotheight);
  clusplot(df, fit$cluster, color=TRUE, shade=TRUE, labels=0, lines=0, main = dataset)
  dev.off()
  
} else if (clustering_type == "hclust") {
  
  d <- dist(as.matrix(df))
  fit <-hclust(d)
  png("cluster_output.png", width=plotwidth, height=plotheight);
  plot(fit, main = dataset)
  dev.off()
  
} else  {
  stop ("enter a valid clustering type")
}



