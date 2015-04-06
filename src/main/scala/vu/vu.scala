import scala.util.Try


package object vu {

  def make[T](p: => T)(s: T => Unit) = { val v = p; s(v); v }

  def using[T <: { def close() }, R](p: => T)(s: T => R): R = { val r = s(p); Try(p.close()); r }
}
