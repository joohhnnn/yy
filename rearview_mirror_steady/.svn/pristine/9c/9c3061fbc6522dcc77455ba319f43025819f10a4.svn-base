package com.txznet.plugin.resolution

class Resolution {
    // specifies resource dir of the android project, layout resource files will be generated
    // under this location
    String resDir = "src/main/res"
    // specifies design size of the android project, in pixels
    int designWidth = 800
    int designHeight = 480
    // file name for resource xml
    String resFileNameX = "lay_x.xml"
    String resFileNameY = "lay_y.xml"
    // xml attribute id for resources, must contain a {0} placeholder, placeholder will be
    // replaced by id position while generating
    String attrNamePatternX = "x{0}"
    String attrNamePatternY = "y{0}"
    // Closure for setting layout resources dir name
    // the Closure must accepts 2 parameters (screenWidth, screenHeight)
    Closure resDirNameClosure

    ArrayList<ResolutionInfo> supportedResolution = new ArrayList<ResolutionInfo>()

    /**
     * adds a resolution definition to support list
     * @param resolution resolution info, e.g. 1920x1080
     * @param autoGenerate
     * @return
     */
    def supportResolution(String resolution, boolean autoGenerate = true) {
        String[] split = resolution.split("x")
        supportResolution(split[0] as int, split[1] as int, autoGenerate)
    }

    /**
     * adds a resolution definition to support list
     * @param screenWidth screen width in pixels
     * @param screenHeight screen height in pixels
     * @param autoGenerate generate layout resource files if resource for this resolution is
     *        missing
     * @return
     */
    def supportResolution(int screenWidth, int screenHeight, boolean autoGenerate = true) {
        supportedResolution << new ResolutionInfo(screenWidth: screenWidth,
                screenHeight: screenHeight, autoGenerate: autoGenerate)
    }

    /**
     * set design size, in pixels
     * @param size size str divided by 'x', e.g. 800x480
     * @return
     */
    def designSize(String size) {
        String[] split = size.split("x")

        designSize(split[0] as int, split[1] as int)
    }

    /**
     * set design size, in pixels
     * @param designWidth design size width
     * @param designHeight design size height
     * @return
     */
    def designSize(int designWidth, int designHeight) {
        this.designWidth = designWidth
        this.designHeight = designHeight
    }

    /**
     * specify a Closure to generate resource folder name for resolution resources, for each
     * resolution the screenWidth and screenHeight will be passed to this Closure
     * @param c the Closure for generating folder name for each resolution
     * @return
     */
    def resDirName(Closure c) {
        resDirNameClosure = c
    }

    def generateResources(String rootDir) {
        supportedResolution.each {info ->
            File f = new File(rootDir + File.separator + resDir + File.separator +
                    resDirNameClosure.call(info.screenWidth, info.screenHeight))
            if (!f.exists()) {
                println "resolution ${info.screenWidth} x ${info.screenHeight}" +
                        "is missing, auto generate = ${info.autoGenerate}"

                if(info.autoGenerate) {
                    f.mkdirs()
                    new ResolutionResFileGenerator().generateResFiles(designWidth, designHeight,
                            info.screenWidth, info.screenHeight, f.path, resFileNameX, resFileNameY,
                            attrNamePatternX, attrNamePatternY)
                }
            } else {
                println "resolution ${info.screenWidth} x ${info.screenHeight}" +
                        " already supported."
            }
        }
    }
}