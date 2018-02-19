
import org.apache.spark.sql.{Row, SQLContext, SaveMode, SparkSession}
import org.apache.spark.{SparkConf, SparkContext, rdd}

object task1{
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
      .setAppName("task1")
      .setMaster("local[*]")//Create configuration object
    val sc = new SparkContext(conf)// create SparkContext object

    val fileDir = args(0)//"input/reviews_Toys_and_Games_5.json" //reviews_Toys_and_Games_5

    val sqlContext = new SQLContext(sc)
    val df1 = sqlContext.read.json(fileDir) // Read JSON directly

    val spark = SparkSession
      .builder()
      .appName("task1")
      .config("spark.some.config.option", "some-value")//      .config("spark.some.config.option", "some-value")
      .getOrCreate()
//    val df1 = spark.read.json(fileDir) //df1 is a DataFrame!!

    df1.createOrReplaceTempView("rating")
    val rating = spark.sql("SELECT asin, avg(overall) as rating_avg FROM rating GROUP BY asin ORDER BY asin ASC")



    import java.io._

    def printToFile(f: java.io.File)(op: java.io.PrintWriter => Unit)
    {
      val p = new java.io.PrintWriter(f);
      p.write("asin,")
      p.write("rating_avg\n")
      try { op(p) }
      finally { p.close() }
    }

    val avgs = rating.rdd
      .map( t=> (t(0),t(1)).toString().replaceAll("\\(","").replaceAll("\\)",""))
      .collect()


    printToFile(new File(args(1))) { //"output/Wanjin_Li_task1.csv"
      p => avgs.foreach(p.println) // avgs.foreach(p.println)
    }

    rating.show()


  }
}