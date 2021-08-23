package com.txznet.plugin.resolution

/**
 * File generator for resolution resources
 * Created by J on 2018/6/25.
 */

class ResolutionResFileGenerator {
    def xmlHeader = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<resources>\n"
    def xmlFooter = "</resources>"
    def xmlAttrPattern = "<dimen name=\"{id}\">{value}</dimen>"

    /**
     * generate layout resource file for specified resolution
     *
     * @param designWidth
     * @param designHeight
     * @param screenWidth
     * @param screenHeight
     * @param dir
     * @param filePatternX
     * @param filePatternY
     * @param idPatternX
     * @param idPatternY
     * @return
     */
    def generateResFiles(int designWidth, int designHeight, int screenWidth, int screenHeight,
                         String dir, String filePatternX, String filePatternY, String idPatternX,
                         String idPatternY) {
        String widthResFileName = dir + File.separator + filePatternX
        String heightResFileName = dir + File.separator + filePatternY

        generateResFile(designWidth, screenWidth, widthResFileName, idPatternX)
        generateResFile(designHeight, screenHeight, heightResFileName, idPatternY)
    }

    def generateResFile(int idCount, int dimenTotal, String filename, String idPattern) {
        float cell = dimenTotal * 1.0f / idCount

        def writer = new FileWriter(filename)

        // write xml header
        writer.write xmlHeader

        // write id attributes
        for (i in 1..idCount) {
            writer.write(xmlAttrPattern
                    .replaceFirst("\\{id\\}" as String, idPattern.replaceFirst("\\{0\\}", i as String))
                    .replaceFirst("\\{value\\}", "${String.format("%.2f", (cell * i))}px"))
            writer.write("\n")
        }

        // write xml footer
        writer.write xmlFooter

        writer.flush()
        writer.close()
    }
}
