package testapp.models

import groovy.transform.ToString
import sybrix.easygsp2.db.transforms.FBEntity

@ToString(includeFields = true,includeNames = true)
@FBEntity
class Game {

        Game(){
                acceptingTrades = false
        }

        //private def dynamicProperties = []
        static primaryKeys= ['gameId']

        def static columns = [gameActive:'is_active']

        Long gameId
        Long gameSystemId
        Long gameTitleId

        String smallImageUrl = ''
        String largeImageUrl = ''
        String mediumImageUrl = ''

        Integer quantityInStock = 0
        Integer maxTradeCount = 0

        Double weight = 0.0
        String weightUnits

        BigDecimal tradeForCreditPrice = 0.0 // trade in value
        Double tradeForCashPrice = 0.0 // sell game

        Double cost = 0.0
        Double retailPrice = 0.0

        String upcCode = ''
        Boolean acceptingTrades = false

        Date created
        Date lastModified  = new Date()
        Integer sortOrder = 0
        Boolean gameActive = true
        String asin
        String amazonAffiliateUrl

        Boolean manualTradePricing
        Date lastTradePriceUpdateDate
        Date releaseDate

        String defaultImageUrl = ''
        String defaultImageFilePath = ''
        Integer defaultImageHeight
        Integer defaultImageWidth
        Integer largeImageHeight
        Integer largeImageWidth
        String upc
}
