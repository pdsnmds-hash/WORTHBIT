package com.rainbow.priceperuse

object AppInformation {
    private const val publisher = "ALFA.CO SRL"
    private const val address = "PIAZZA 185 REGGIMENTO ARTIGLIERIA FOLGORE 15\n57128 LIVORNO"
    private const val email = "afbvjbnj@gmail.com"
    private const val phone = "+447347076686"
    private const val website = "https://nivorixa.info/"

    private fun publisherDetails(italian: Boolean) = listOf(
        publisher, address, if (italian) "Italia (IT)" else "Italy (IT)",
        "Email: $email", "${if (italian) "Telefono" else "Phone"}: $phone",
        "${if (italian) "Sito dell’editore" else "Publisher website"}: $website"
    ).joinToString("\n")

    fun about(italian: Boolean) = (if (italian) "WorthBit ti aiuta a capire quanto costa davvero ogni utilizzo delle tue cose.\n\nAggiungi una cosa, indica il prezzo di acquisto e tocca «Utilizzato» ogni volta che la usi. Il costo per utilizzo è il prezzo diviso per il numero di utilizzi, arrotondato a due decimali.\n\nPuoi annullare l’ultimo tocco, modificare le cose e scegliere tra 30 icone e tre temi. Tutti i dati restano sul dispositivo. I prezzi sono espressi in euro." else "WorthBit helps you see what each use of your belongings costs.\n\nAdd an item and its purchase price, then tap ‘Used it’ whenever you use it. Cost per use is the purchase price divided by the number of uses, rounded to two decimal places.\n\nUndo the last tap, edit your things, and choose from 30 icons and three themes. All data stays on your device. Prices are in euros.") + "\n\n" +
        (if (italian) "Pubblicata da $publisher\nSito dell’editore: $website" else "Published by $publisher\nPublisher website: $website")

    fun privacy(italian: Boolean) = if (italian) """
        Informativa sulla privacy
        Ultimo aggiornamento: 8 novembre 2026

        Dati salvati
        WorthBit salva i nomi delle cose inseriti da te, i prezzi di acquisto, le icone scelte, il numero di utilizzi e le preferenze di lingua e tema. A ogni cosa viene assegnato un identificatore casuale locale.

        Dove e perché
        Queste informazioni sono conservate nell’archivio privato dell’app sul dispositivo e servono esclusivamente a mostrare il tuo inventario, calcolare il costo per utilizzo e ricordare le tue preferenze. Non è richiesto un account.

        Nessuna raccolta remota
        Questa versione non dispone di server, pubblicità, strumenti di analisi o tracciamento. Non invia i dati a noi o a terzi. L’app non richiede accesso a Internet, posizione, fotocamera, microfono, contatti o file personali. Non raccoglie identificatori pubblicitari.

        Sito esterno e richieste di assistenza
        Il link al sito dell’editore si apre nel browser esterno; WorthBit non invia il tuo inventario al sito. La navigazione sul sito è regolata dalla sua informativa sulla privacy. Se ci contatti via email o telefono, riceviamo i dati che scegli di condividere e li usiamo per gestire la tua richiesta. L’app non allega automaticamente i tuoi dati. Le comunicazioni di assistenza sono distinte dai dati conservati esclusivamente sul dispositivo.

        Conservazione e cancellazione
        I dati restano sul dispositivo finché non elimini le cose o cancelli i dati dell’app nelle impostazioni Android. Eliminando una cosa vengono eliminati il relativo prezzo, icona e contatore. La disinstallazione rimuove l’archivio locale. Il backup automatico Android dell’app è disabilitato. L’app non offre sincronizzazione o esportazione.

        Il tuo controllo
        Puoi consultare, modificare ed eliminare le tue cose nell’app e cambiare lingua o tema in qualsiasi momento. Non esistono copie sui nostri server da richiedere o cancellare. Evita di inserire informazioni sensibili nei nomi delle cose.

        Dispositivo e sistema operativo
        Le funzionalità di Android, della tastiera scelta e di eventuali strumenti di backup o gestione del dispositivo sono regolate dalle rispettive informative. La sicurezza del dispositivo dipende anche dalle tue impostazioni.

        Modifiche
        Se le funzionalità relative ai dati cambieranno, questa informativa sarà aggiornata nell’app. Questa informativa descrive esclusivamente la versione attuale di WorthBit.

        Editore e contatti per la privacy
        ${publisherDetails(true).replace("\n", "\n        ")}
    """.trimIndent() else """
        Privacy policy
        Last updated: November 8, 2026

        Stored information
        WorthBit stores the item names you enter, purchase prices, chosen icons, use counts, and your language and theme preferences. Each item is assigned a random local identifier.

        Where and why
        This information is kept in the app’s private storage on your device, solely to display your inventory, calculate cost per use, and remember your preferences. No account is required.

        No remote collection
        This version has no servers, advertising, analytics, or tracking tools. It does not send your data to us or third parties. The app does not request Internet, location, camera, microphone, contacts, or personal file access. It does not collect advertising identifiers.

        External website and support requests
        The publisher website link opens in your external browser; WorthBit does not send your inventory to the website. Website browsing is governed by that website’s privacy policy. If you contact us by email or phone, we receive the information you choose to share and use it to handle your request. The app does not automatically attach your data. Support correspondence is separate from the data stored only on your device.

        Retention and deletion
        Data stays on your device until you delete items or clear the app’s data in Android settings. Deleting an item removes its price, icon, and counter. Uninstalling removes the local storage. Android automatic backup is disabled for this app. The app does not offer synchronization or export.

        Your control
        You can view, edit, and delete your items in the app and change language or theme at any time. There are no copies on our servers to request or delete. Avoid entering sensitive information in item names.

        Device and operating system
        Android features, your chosen keyboard, and any device backup or management tools are governed by their own policies. Device security also depends on your settings.

        Changes
        If features involving data change, this policy will be updated in the app. This policy describes only the current version of WorthBit.

        Publisher and privacy contact
        ${publisherDetails(false).replace("\n", "\n        ")}
    """.trimIndent()
}
