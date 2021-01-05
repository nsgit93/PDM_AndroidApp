package core

data class Notification(val type:String,val payload:Any) {
}
data class RemoveNotification(val _id:String)