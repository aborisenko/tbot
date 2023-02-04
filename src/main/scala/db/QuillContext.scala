package db

import io.getquill.{Literal, MysqlZioJdbcContext, NamingStrategy, SnakeCase}
import io.getquill.jdbczio.Quill
import zio.ZLayer

import javax.sql.DataSource

object QuillContext extends MysqlZioJdbcContext(NamingStrategy(SnakeCase, Literal)){
    val dataSourceLayer: ZLayer[Any, Throwable, DataSource] = Quill.DataSource.fromPrefix("ctx")
    val quillLayer: ZLayer[DataSource, Nothing, Quill.Mysql[SnakeCase.type]] = Quill.Mysql.fromNamingStrategy(SnakeCase)
}
