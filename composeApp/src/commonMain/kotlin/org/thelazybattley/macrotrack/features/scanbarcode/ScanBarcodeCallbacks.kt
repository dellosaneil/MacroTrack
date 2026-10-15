package org.thelazybattley.macrotrack.features.scanbarcode

interface ScanBarcodeCallbacks {
    fun onBarcodeDetected(raw: String)
    fun onOpenManualEntry()
    fun onManualBarcodeChanged(value: String)
    fun onLookUpManualBarcode()
    fun onScanAgain()
    fun onToggleTorch()
    fun onAmountChanged(amount: Double)
    fun onAddToMeal()
    fun onLater()
    fun onEditFood()
    fun onCreateFood()
    fun resetNavigateScreen()

    companion object {
        fun default() = object : ScanBarcodeCallbacks {
            override fun onBarcodeDetected(raw: String) {}
            override fun onOpenManualEntry() {}
            override fun onManualBarcodeChanged(value: String) {}
            override fun onLookUpManualBarcode() {}
            override fun onScanAgain() {}
            override fun onToggleTorch() {}
            override fun onAmountChanged(amount: Double) {}
            override fun onAddToMeal() {}
            override fun onLater() {}
            override fun onEditFood() {}
            override fun onCreateFood() {}
            override fun resetNavigateScreen() {}
        }
    }
}
