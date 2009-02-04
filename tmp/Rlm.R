lmFitConverter <-
function(obj,...)
{
 .JNew("sjava2.RLinearModelFit",
 obj$coefficients, obj$residuals,
 obj$fitted, obj$rank, obj$df.residual)
}
lmPredictConverter <- function(preds,...) {
    .JNew("sjava2.RLinearModelPredict",
    preds$fit[,1], preds$se.fit, preds$fit[,2], preds$fit[,3],
    preds$df, preds$residual.scale)
}
setJavaFunctionConverter(lmFitConverter, function(x,...){inherits(x,"lm")},
                          description="lm fit object to Java",
                          fromJava=F)
setJavaFunctionConverter(lmPredictConverter, function(x,...){inherits(x,"lmprediction")},
                          description="lm predict object to Java",
                          fromJava=F)
                          
buildLM <- function(modelname, x,y) {
    # x will come in as a double[][]
    x <- matrix(unlist(x), ncol=length(x))

    # assumes y ~ all columns of x
    d <- data.frame(y=y,x)
    fmla <- paste(names(d)[-1],sep="",collapse="+")
    fmla <- formula(paste("y",fmla,sep="~",collapse=""))

    assign(modelname, lm(fmla, d), pos=1)
    get(modelname)
}
predictLM <- function( modelname, newx, interval) {
    newx <- data.frame( matrix(unlist(newx), ncol=length(newx)) )
    print(interval)
    if (interval == '' || !(interval %in% c('confidence','prediction')) ) { 
        interval = 'confidence'
    }
    preds <- predict( get(modelname), newx, se.fit = TRUE, interval=interval);
    class(preds) <- 'lmprediction'
    preds
}
deleteLM <- function(modelname) {
    print(modelname)
    rm(c(modelname,'rmse'),pos=1)
}
