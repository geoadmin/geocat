handlers.add name: 'full xml', select: 'che:CHE_MD_Metadata', { el ->
    // Don't need a return because last expression of a function is
    // always returned in groovy
    el
}