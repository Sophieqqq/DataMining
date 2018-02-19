
import org.apache.spark.sql.{Row, SQLContext, SaveMode, SparkSession}
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext

object task2{
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
      .setAppName("task2")
      .setMaster("local[*]")//Create configuration object
    val sc = new SparkContext(conf)// create SparkContext object
    val fileDir =  args(0)//"input/reviews_Toys_and_Games_5.json"
    val fileDir2 =  args(1)//"input/metadata.json"

//    val sqlContext = new SQLContext(sc)
//    val df1 = sqlContext.read.json(fileDir) // Read JSON directly
//    val df2 = sqlContext.read.json(fileDir2) // Read JSON directly

    val spark = SparkSession
      .builder()
      .appName("task2")
      .config("spark.some.config.option", "some-value")
      .getOrCreate()
    val df1 = spark.read.json(fileDir) //df1 is a DataFrame!!
    val df2 = spark.read.json(fileDir2) //df2 is a DataFrame!!

    df1.createOrReplaceTempView("rating")
    df2.createOrReplaceTempView("brands")

    val rating = spark.sql("SELECT asin, overall FROM rating")
    val brand = spark.sql("SELECT asin, brand FROM brands WHERE brand <> '' and brand IS NOT null")
    val all = rating.join(brand,rating("asin")=== brand("asin")).createOrReplaceTempView("all_toys")
    val toys = spark.sql("SELECT brand, avg(overall) as rating_avg FROM all_toys GROUP BY brand ORDER BY brand ASC")



    import java.io._

    def printToFile(f: java.io.File)(op: java.io.PrintWriter => Unit)
    {
      val p = new java.io.PrintWriter(f);
      p.write("brand,")
      p.write("rating_avg\n")
      try { op(p) }
      finally { p.close() }
    }

    printToFile(new File(args(2))) { //"output/Wanjin_Li_task2.csv"

      p => toys.rdd
        .map( t=> (t(0),t(1)).toString().replaceAll("\\(","").replaceAll("\\)",""))
        .collect().foreach(p.println)
    }
    toys.show()

  }
}