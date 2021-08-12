package com.example.mpgievents

class Events {
    var date:String?=null
    var fees:String?=null
    var title:String?=null
    var uploadDate:String?=null
    var uploadTime:String?=null
    var randomName:String?=null

    constructor() {}

    constructor(
        date:String?,
        fees:String?,
        title:String?,
        uploadDate:String?,
        uploadTime:String?,
        randomName:String?
    )
    {
        this.date=date
        this.fees=fees
        this.title=title
        this.uploadDate=uploadDate
        this.uploadTime=uploadTime
        this.randomName=randomName
    }
}