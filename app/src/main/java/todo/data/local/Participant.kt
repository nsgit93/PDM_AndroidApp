package todo.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "participants")
data class Participant(
    @PrimaryKey(autoGenerate = true)
    val _id: Int,
    @ColumnInfo(name="name")
    var name: String,
    @ColumnInfo(name = "age")
    var age: String,
    @ColumnInfo(name = "participantionsNo")
    var participationsNo: String
) {
    override fun toString(): String = "Name: "+name+" Age: "+age+" Participations: "+participationsNo
}
