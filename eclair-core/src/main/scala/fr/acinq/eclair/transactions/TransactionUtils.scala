package fr.acinq.eclair.transactions

import fr.acinq.bitcoin.{Crypto, LexicographicalOrdering, OutPoint, Satoshi, Script, ScriptElt, Transaction, TxIn, TxOut}
import fr.acinq.eclair.CltvExpiry
import scodec.bits.ByteVector

import scala.annotation.tailrec

object TransactionUtils {

  def isLessThan(a: OutPoint, b: OutPoint): Boolean = {
    if (a.txid == b.txid) a.index < b.index
    else lexicographicalOrder(a.txid, b.txid) < 0
  }

  def isLessThan(a: TxIn, b: TxIn): Boolean = isLessThan(a.outPoint, b.outPoint)

  def isLessOrCLTV(a: (TxOut, Option[CltvExpiry]), b: (TxOut, Option[CltvExpiry])): Boolean = {
    val amountComparison = a._1.amount.compare(b._1.amount)
    if(amountComparison != 0){
      amountComparison < 0
    } else {
      val lexicographicalComparison = lexicographicalOrder(a._1.publicKeyScript, b._1.publicKeyScript)
      if(lexicographicalComparison == 0 && a._2.isDefined && b._2.isDefined) {
        a._2.get < b._2.get // compare the CLTVs
      } else {
        lexicographicalComparison < 0
      }
    }
  }

  @tailrec
  def lexicographicalOrder(a: ByteVector, b: ByteVector): Int = {
    if (a.isEmpty && b.isEmpty) 0
    else if (a.isEmpty) 1
    else if (b.isEmpty) -1
    else if (a.head == b.head) lexicographicalOrder(a.tail, b.tail)
    else (a.head & 0xff).compareTo(b.head & 0xff)
  }

  /**
    *
    * @param tx input transaction
    * @return the input tx with inputs and outputs sorted in lexicographical order
    */
  def sort(tx: Transaction): Transaction = LexicographicalOrdering.sort(tx)

  /**
    * Returns a tuple with: sorted_transaction and a map of htlc output index and their cltv - applies only to outputs of offered HTLCs
    *
    * @param tx
    * @param outputsWithHtlcCltvInfo the complete list of the outputs of this transaction, enriched with htlc_cltv info
    * @return
    */
  def sortByBIP69AndCLTV(tx: Transaction, outputsWithHtlcCltvInfo:Seq[(TxOut, Option[CltvExpiry])]): (Transaction, Map[Int, CltvExpiry])= {
    assert(tx.txOut.size == outputsWithHtlcCltvInfo.size, "Unable to sort with incomplete information")

    val sortedOutputsAndCltv = outputsWithHtlcCltvInfo.sortWith(isLessOrCLTV)

    val indexCltv = sortedOutputsAndCltv.zipWithIndex.map {
      case ((_, Some(cltv)), index) => (index, cltv)
      case ((_, None), index) => (index, CltvExpiry(-1L))
    }.filterNot(_._2 == CltvExpiry(-1L)).toMap

    val sortedTx = tx.copy(
      txIn = tx.txIn.sortWith(isLessThan),
      txOut = sortedOutputsAndCltv.map(_._1)
    )

    (sortedTx, indexCltv)
  }


}