package com.example.myconnectfour

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import android.opengl.ETC1.getHeight
import android.opengl.ETC1.getWidth
import android.graphics.Color.LTGRAY
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.media.Image
import android.nfc.Tag
import android.util.Log
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TableLayout
import kotlinx.android.synthetic.main.activity_fullscreen.*
import org.w3c.dom.Text
import java.util.*
import kotlin.math.min


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class FullscreenActivity : AppCompatActivity() {

    private var playerOneTurn=true
    private var playerOneClaims= LinkedList<String>()
    private var playerTwoClaims= LinkedList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_fullscreen)
        init()
    }

    private fun init() {
        findViewById<TextView>(R.id.turnDisplay).text="Player Turn: Red"

        for (i in 1..6) {
            for (j in 1..7) {
                val name = "r$i"+"c$j"
                val id = this.resources.getIdentifier(name, "id", this.packageName)
                val myImageView = findViewById<ImageView>(id)
                drawCircle(myImageView, Color.DKGRAY)
            }
        }
    }

    fun reset(@Suppress("UNUSED_PARAMETER")v:View){

        setViewAndChildrenEnabled(findViewById<TableLayout>(R.id.boardLayout), true)

        init()
        playerOneClaims.clear()
        playerTwoClaims.clear()
        playerOneTurn=true
    }

    fun place(v:View) {
        val nodeId: String = resources.getResourceEntryName(v.id)
        val validNodeId = checkColumn(nodeId[3]) //pass the last char of id string (eg. entry name: "r1c1" so '1' is the column value)
        val validNodeString = resources.getResourceEntryName(validNodeId)

        val imgView = findViewById<ImageView>(validNodeId)
        imgView.isEnabled = false

        val myTextView = findViewById<TextView>(R.id.turnDisplay)

        if (playerOneTurn) {
            drawCircle(imgView, Color.RED)
            playerOneClaims.add(validNodeString)
            myTextView.text = "Player Turn: Blue"
        }
        else {
            drawCircle(imgView, Color.BLUE)
            playerTwoClaims.add(validNodeString)
            myTextView.text = "Player Turn: Red"
        }

        //check winner
        checkWinner(Character.getNumericValue(validNodeString[1]), Character.getNumericValue(validNodeString[3]), playerOneTurn)

        //check for a draw then display message
        if(playerOneClaims.count()+playerTwoClaims.count()==42)
        {
            Toast.makeText(this, "Game ends in a Draw!", Toast.LENGTH_LONG).show()
            findViewById<TextView>(R.id.turnDisplay).text="Game ends in a Draw!"
        }

        playerOneTurn = !playerOneTurn
    }

    private fun checkColumn(column:Char):Int{ //claim the bottom-most node in the column that was clicked

        for(i in 6 downTo 1) {
            val name = "r$i" + "c$column"
            val id = this.resources.getIdentifier(name, "id", this.packageName)
            val image = findViewById<ImageView>(id)
            if(image.isEnabled)
                return id
        }
        return 0
    }

    private fun checkWinner(nodeRow:Int, nodeCol:Int, playerOne:Boolean){
        if(checkNodePath(nodeRow, nodeCol, 4, 'd', true, playerOne)
                || checkNodePath(nodeRow, nodeCol, 4, 'e', true, playerOne)
                || checkNodePath(nodeRow, nodeCol, 4, 'f', true, playerOne)
                || checkNodePath(nodeRow, nodeCol, 4, 'g', true, playerOne)) {
            if (playerOne) {
                Toast.makeText(this, "Player One Wins!", Toast.LENGTH_SHORT).show()
                setViewAndChildrenEnabled(findViewById<TableLayout>(R.id.boardLayout), false)
                findViewById<TextView>(R.id.turnDisplay).text="Player One Wins!"
            }
            else {
                Toast.makeText(this, "Player Two Wins!", Toast.LENGTH_SHORT).show()
                setViewAndChildrenEnabled(findViewById<TableLayout>(R.id.boardLayout), false)
                findViewById<TextView>(R.id.turnDisplay).text="Player Two Wins!"
            }
        }
    }

    private fun setViewAndChildrenEnabled(view: View, enabled: Boolean) {
        view.isEnabled = enabled
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val child = view.getChildAt(i)
                setViewAndChildrenEnabled(child, enabled)
            }
        }
    }


    private fun checkNodePath(nodeRow:Int, nodeCol:Int, chipsNeeded:Int, cardinal:Char, forwards:Boolean, playerOne:Boolean):Boolean{
        val id="r"+nodeRow.toString()+"c"+nodeCol.toString()
        var nextRow=0
        var nextCol=0
        if(playerOne){
            if(playerOneClaims.contains(id)){ //node belongs to player 1, continue
                if(chipsNeeded==1)
                    return true
                else{
                    when(cardinal){
                        'a'-> {
                            nextRow = nodeRow - 1
                            nextCol = nodeCol - 1
                        }
                        'b'->{
                            nextRow = nodeRow - 1
                            nextCol = nodeCol
                        }
                        'c'->{
                            nextRow = nodeRow - 1
                            nextCol = nodeCol + 1
                        }
                        'd'->{
                            nextRow = nodeRow
                            nextCol = nodeCol + 1
                        }
                        'e'->{
                            nextRow = nodeRow + 1
                            nextCol = nodeCol + 1
                        }
                        'f'->{
                            nextRow = nodeRow + 1
                            nextCol = nodeCol
                        }
                        'g'->{
                            nextRow = nodeRow + 1
                            nextCol = nodeCol - 1
                        }
                        'h'->{
                            nextRow = nodeRow
                            nextCol = nodeCol - 1
                        }
                    }
                    if(nextRow <= 0 || nextRow >= 7 || nextCol <=0 || nextCol >=8) { //if out of bounds then change directions
                        val nodeSkip=4-chipsNeeded
                        var oppositeDirection=cardinal
                        when (cardinal) {
                            'a' -> {
                                oppositeDirection = 'e'
                                nextRow = nodeRow + 1 + nodeSkip
                                nextCol = nodeCol + 1 + nodeSkip
                            }
                            'b' -> {
                                oppositeDirection = 'f'
                                nextRow = nodeRow + 1 + nodeSkip
                                nextCol = nodeCol
                            }
                            'c' -> {
                                oppositeDirection = 'g'
                                nextRow = nodeRow + 1 + nodeSkip
                                nextCol = nodeCol - 1 - nodeSkip
                            }
                            'd' -> {
                                oppositeDirection = 'h'
                                nextRow = nodeRow
                                nextCol = nodeCol - 1 - nodeSkip
                            }
                            'e' -> {
                                oppositeDirection = 'a'
                                nextRow = nodeRow - 1 - nodeSkip
                                nextCol = nodeCol - 1 - nodeSkip
                            }
                            'f' -> {
                                oppositeDirection = 'b'
                                nextRow = nodeRow - 1 - nodeSkip
                                nextCol = nodeCol
                            }
                            'g' -> {
                                oppositeDirection = 'c'
                                nextRow = nodeRow - 1 - nodeSkip
                                nextCol = nodeCol + 1 + nodeSkip
                            }
                            'h' -> {
                                oppositeDirection = 'd'
                                nextRow = nodeRow
                                nextCol = nodeCol + 1 + nodeSkip
                            }
                        }
                        return if (nextRow <= 0 || nextRow >= 7 || nextCol <= 0 || nextCol >= 8) //if out of bounds
                            false
                        else
                            checkNodePath(nextRow, nextCol, chipsNeeded-1, oppositeDirection, !forwards, playerOne)
                    }
                    else
                        return checkNodePath(nextRow,nextCol, chipsNeeded-1, cardinal,forwards,playerOne)
                }
            } //end of player one node check
            else { //node does not belong to player 1, stop checking in current direction and go opposite direction check
                if (forwards){ //if this instance is heading forward direction, then check in opposite direction
                    var oppositeDirection = cardinal
                    /*
                    'a'=NW
                    'b'=N
                    'c'=NE
                    'd'=E
                    'e'=SE
                    'f'=S
                    'g'=SW
                    'h'=W
                    */
                    val nodeSkip=4-chipsNeeded
                    when (cardinal) {
                        'a' -> {
                            oppositeDirection = 'e'
                            nextRow = nodeRow + 1 + nodeSkip
                            nextCol = nodeCol + 1 + nodeSkip
                        }
                        'b' -> {
                            oppositeDirection = 'f'
                            nextRow = nodeRow + 1 + nodeSkip
                            nextCol = nodeCol
                        }
                        'c' -> {
                            oppositeDirection = 'g'
                            nextRow = nodeRow + 1 + nodeSkip
                            nextCol = nodeCol - 1 - nodeSkip
                        }
                        'd' -> {
                            oppositeDirection = 'h'
                            nextRow = nodeRow
                            nextCol = nodeCol - 1 - nodeSkip
                        }
                        'e' -> {
                            oppositeDirection = 'a'
                            nextRow = nodeRow - 1 - nodeSkip
                            nextCol = nodeCol - 1 - nodeSkip
                        }
                        'f' -> {
                            oppositeDirection = 'b'
                            nextRow = nodeRow - 1 - nodeSkip
                            nextCol = nodeCol
                        }
                        'g' -> {
                            oppositeDirection = 'c'
                            nextRow = nodeRow - 1 - nodeSkip
                            nextCol = nodeCol + 1 + nodeSkip
                        }
                        'h' -> {
                            oppositeDirection = 'd'
                            nextRow = nodeRow
                            nextCol = nodeCol + 1 + nodeSkip
                        }
                    }
                    return if (nextRow <= 0 || nextRow >= 7 || nextCol <= 0 || nextCol >= 8) //if out of bounds
                        false
                    else
                        checkNodePath(nextRow, nextCol, chipsNeeded, oppositeDirection, !forwards, playerOne)
                }
                else { //this instance is checking in opposite direction already, don't check opposite direction (the original direction) again
                    return false
                }
            } //end of player one opposite direction check
        } //end of player one checks
        else{ //player two check
            if(playerTwoClaims.contains(id)){
                if(chipsNeeded==1)
                    return true
                else{
                    when(cardinal){
                        'a'-> {
                            nextRow = nodeRow - 1
                            nextCol = nodeCol - 1
                        }
                        'b'->{
                            nextRow = nodeRow - 1
                            nextCol = nodeCol
                        }
                        'c'->{
                            nextRow = nodeRow - 1
                            nextCol = nodeCol + 1
                        }
                        'd'->{
                            nextRow = nodeRow
                            nextCol = nodeCol + 1
                        }
                        'e'->{
                            nextRow = nodeRow + 1
                            nextCol = nodeCol + 1
                        }
                        'f'->{
                            nextRow = nodeRow + 1
                            nextCol = nodeCol
                        }
                        'g'->{
                            nextRow = nodeRow + 1
                            nextCol = nodeCol - 1
                        }
                        'h'->{
                            nextRow = nodeRow
                            nextCol = nodeCol - 1
                        }
                    }
                    if(nextRow <= 0 || nextRow >= 7 || nextCol <=0 || nextCol >=8) { //if out of bounds
                        var oppositeDirection=cardinal
                        val nodeSkip=4-chipsNeeded
                        when (cardinal) {
                            'a' -> {
                                oppositeDirection = 'e'
                                nextRow = nodeRow + 1 + nodeSkip
                                nextCol = nodeCol + 1 + nodeSkip
                            }
                            'b' -> {
                                oppositeDirection = 'f'
                                nextRow = nodeRow + 1 + nodeSkip
                                nextCol = nodeCol
                            }
                            'c' -> {
                                oppositeDirection = 'g'
                                nextRow = nodeRow + 1 + nodeSkip
                                nextCol = nodeCol - 1 - nodeSkip
                            }
                            'd' -> {
                                oppositeDirection = 'h'
                                nextRow = nodeRow
                                nextCol = nodeCol - 1 - nodeSkip
                            }
                            'e' -> {
                                oppositeDirection = 'a'
                                nextRow = nodeRow - 1 - nodeSkip
                                nextCol = nodeCol - 1 - nodeSkip
                            }
                            'f' -> {
                                oppositeDirection = 'b'
                                nextRow = nodeRow - 1 - nodeSkip
                                nextCol = nodeCol
                            }
                            'g' -> {
                                oppositeDirection = 'c'
                                nextRow = nodeRow - 1 - nodeSkip
                                nextCol = nodeCol + 1 + nodeSkip
                            }
                            'h' -> {
                                oppositeDirection = 'd'
                                nextRow = nodeRow
                                nextCol = nodeCol + 1 + nodeSkip
                            }
                        }
                        return if (nextRow <= 0 || nextRow >= 7 || nextCol <= 0 || nextCol >= 8) //if out of bounds
                            false
                        else
                            checkNodePath(nextRow, nextCol, chipsNeeded-1, oppositeDirection, !forwards, playerOne)
                    }
                    else
                        return checkNodePath(nextRow,nextCol, chipsNeeded-1, cardinal,forwards,playerOne)
                }
            }
            else{ //node does not belong to player 2
                if(forwards)
                {
                    var oppositeDirection = cardinal
                    /*
                    'a'=NW
                    'b'=N
                    'c'=NE

                    'd'=E
                    'e'=SE
                    'f'=S
                    'g'=SW
                    'h'=W
                    */

                    val nodeSkip=4-chipsNeeded
                    when (cardinal) {
                        'a' -> {
                            oppositeDirection = 'e'
                            nextRow = nodeRow + 1 + nodeSkip
                            nextCol = nodeCol + 1 + nodeSkip
                        }
                        'b' -> {
                            oppositeDirection = 'f'
                            nextRow = nodeRow + 1 + nodeSkip
                            nextCol = nodeCol
                        }
                        'c' -> {
                            oppositeDirection = 'g'
                            nextRow = nodeRow + 1 + nodeSkip
                            nextCol = nodeCol - 1 - nodeSkip
                        }
                        'd' -> {
                            oppositeDirection = 'h'
                            nextRow = nodeRow
                            nextCol = nodeCol - 1 - nodeSkip
                        }
                        'e' -> {
                            oppositeDirection = 'a'
                            nextRow = nodeRow - 1 - nodeSkip
                            nextCol = nodeCol - 1 - nodeSkip
                        }
                        'f' -> {
                            oppositeDirection = 'b'
                            nextRow = nodeRow - 1 - nodeSkip
                            nextCol = nodeCol
                        }
                        'g' -> {
                            oppositeDirection = 'c'
                            nextRow = nodeRow - 1 - nodeSkip
                            nextCol = nodeCol + 1 + nodeSkip
                        }
                        'h' -> {
                            oppositeDirection = 'd'
                            nextRow = nodeRow
                            nextCol = nodeCol + 1 + nodeSkip
                        }
                    }
                    return if (nextRow <= 0 || nextRow >= 7 || nextCol <= 0 || nextCol >= 8) //if out of bounds
                        false
                    else
                        checkNodePath(nextRow, nextCol, chipsNeeded, oppositeDirection, !forwards, playerOne)
                }
                else
                    return false
            }
        } //end player 2 check
    }


    private fun drawCircle(mImageView: ImageView, nextColor: Int){

        val height=200
        val width=200

        val bitmap = Bitmap.createBitmap(
                width, // Width
                height, // Height
                Bitmap.Config.ARGB_8888 // Config
        )

        // Initialize a new Canvas instance
        val canvas = Canvas(bitmap)

        // Draw a solid color to the canvas background
        canvas.drawColor(0x0099CC)

        // Initialize a new Paint instance to draw the Circle
        val paint = Paint()
        paint.style = Paint.Style.FILL
        paint.color = nextColor
        paint.isAntiAlias = true

        // Calculate the available radius of canvas
        val radius = min(canvas.width, canvas.height / 2)

        // Set a pixels value to padding around the circle
        val padding = 5f

        /*
                    public void drawCircle (float cx, float cy, float radius, Paint paint)
                        Draw the specified circle using the specified paint. If radius is <= 0, then
                        nothing will be drawn. The circle will be filled or framed based on the
                        Style in the paint.

                    Parameters
                        cx : The x-coordinate of the center of the circle to be drawn
                        cy : The y-coordinate of the center of the circle to be drawn
                        radius : The radius of the cirle to be drawn
                        paint : The paint used to draw the circle
                */
        // Finally, draw the circle on the canvas
        canvas.drawCircle(
                canvas.width/2f, // cx
                canvas.height/2f, // cy
                radius - padding, // Radius
                paint // Paint
        )

        // Display the newly created bitmap on app interface
        mImageView.setImageBitmap(bitmap)

    }
}
