import sys, os, string, shutil, json, codecs
import xml.dom.minidom as xml
from chameleon_build_comm import *
from AndroidManifest import AndroidManifestInst

def error(s):
    print >> sys.stderr, s

def modifyManifest(channel, libs, manifestFilePath):
    projectJsonPath = os.path.join('chameleon', 'channels', channel, 
            'project.json')
    with codecs.open(projectJsonPath, 'r') as f:
        obj = json.load(f)
    sc = obj.get("splashscreen")
    icons = obj.get("icons")
    cppath = manifestFilePath+'.orig'
    if os.path.exists(cppath) and not isNewerThan(manifestFilePath, cppath):
        return
    manifestInst = loadManifest(manifestFilePath)
    mergeLibManifests(libs, manifestInst)
    if sc is not None:
        manifestInst.replaceEntryActivity()
    if icons is not None:
        manifestInst.setIcon('chameleon_icon')
    manifestInst.safeDump(manifestFilePath)
    shutil.copyfile(manifestFilePath, cppath)

def loadManifest(path):
    return AndroidManifestInst(path)

def mergeLibManifests(libs,  manifestInst):
    for l in libs:
        mergeSingleLibManifest(l,  manifestInst)

def mergeSingleLibManifest(lib, manifestInst):
    libManifestInst = loadLibManifest(lib)
    mergeLibManifestInst(lib.cfg, libManifestInst, manifestInst)

def loadLibManifest(lib):
    p = os.path.join(lib.path, 'AndroidManifest.xml.template')
    return AndroidManifestInst(p)

def mergeLibManifestInst(cfg, libManifestInst, manifestInst):
    libManifestInst.replace(dict([(name[1:], value) for name, value in cfg.items()]))
    manifestInst.merge(libManifestInst)

def makeBooleanValue(name, val):
    if val: 
        return 'bundle.putBoolean("%s", true);' %name 
    else:
        return 'bundle.putBoolean("%s", false);' %name

TYPE_BUNDLE_CFG = {
        's' : lambda name, val: 'bundle.putString("%s", "%s");' %(name, val),
        'l' : lambda name, val: 'bundle.putLong("%s", %s);' %(name, val),
        'b' : makeBooleanValue, 
        'f' : lambda name, val: 'bundle.putFloat("%s", %s);' %(name, val),
        'h' : lambda name, val: ''
    }

SINGLE_LIB_TEMPLATE = string.Template("""
package prj.chameleon.channelapi;

import android.os.Bundle;

$importstrs

public class Instantializer implements IInstantializer{
 {

    @Override
    public void initChameleon() {

        ApiCommonCfg commCfg = new ApiCommonCfg();
        commCfg.mAppName = "$appName";
        commCfg.mChannel = "$channel";
        commCfg.mIsLandscape = $landscape;
        commCfg.mIsDebug = $debug;
        ChannelInterface.setChannelName("$channel");
        
        $initfuncs
    }

    $initfuncbodys
}
""")


def getAPIType(t):
    ts = t.split(',')
    ts = map(lambda x : 'Constants.PluginType.%s_API' %x.upper(), ts)
    return '|'.join(ts)

def genBundleCfg(libAPIImp, t, cfg):
    x = ['Bundle bundle = new Bundle();']
    for name, val in cfg.items():
        typeChar = name[0]
        realName = name[1:]
        x.append(TYPE_BUNDLE_CFG[typeChar](realName, val))
    x += ['%s api = new %s();' %(libAPIImp, libAPIImp), 
        'api.initCfg(commCfg, bundle);',
        'ChannelInterface.addApiGroup(new APIGroup(%s, api));' %getAPIType(t)]
    return '\tprivate void init%s(ApiCommonCfg commCfg) {\n' %libAPIImp+ '\n\t\t'.join(x) + '\n\t}'

def checkDependency(genFilePath, depends):
    if not os.path.exists(genFilePath):
        return True
    for d in depends:
        if isNewerThan(d, genFilePath):
            return True
    return False

def doGenInstantializer(channel, globalcfg, libs):
    libStrs = [genLibInstantializer(l) for l in libs]
    importStrs = '\t\t\n'.join([x[0] for x in libStrs])
    initfuncs = '\t\t\n'.join([x[1] for x in libStrs])
    initFuncBody = '\t\n'.join([x[2] for x in libStrs])
    landscape = 'false'
    if globalcfg['blandscape']:
        landscape = 'true'
    content = SINGLE_LIB_TEMPLATE.substitute(lib=l.name, 
            landscape=landscape, appName=globalcfg['sappname'],
            channel=channel, debug='true',
            importstrs=importStrs, initfuncs=initfuncs,
            initfuncbodys = initFuncBody)
    return content

def genInstantializer(channel, genPath, globalcfg, libs):
    content = doGenInstantializer(channel, globalcfg, libs)
    genPkgPath = os.path.join(genPath, 'prj', 'chameleon', 'channelapi')
    genFilePath = os.path.join(genPkgPath, 'Instantializer.java')
    projectJsonPath = os.path.join('chameleon', 'channels', channel, 
            'project.json')
    depends = [projectJsonPath] + [x.cfgpath for x in libs]
    if not checkDependency(genFilePath, depends):
        error('ignore gen, no modification found')
        return
    content = doGenInstantializer(channel, globalcfg, libs)
    if not os.path.exists(genPkgPath):
        os.makedirs(genPkgPath)
    with codecs.open(genFilePath, 'w', 'utf-8') as f:
        f.write(content)

def genLibInstantializer(l):
    libAPIImp =l.name[0].upper()+l.name[1:]+'ChannelAPI'
    bundleStr = genBundleCfg(libAPIImp, l.type, l.cfg)
    return ('import prj.chameleon.%s.%s' %(l.name, libAPIImp), 'init%s(commCfg);' %libAPIImp, bundleStr)

def isNewerThan(a, b):
    return os.path.getmtime(a) > os.path.getmtime(b)

def genRFileForPkgName(channel, genPath, pkgName):
    s = pkgName.split('.')
    d = os.path.join(*([genPath] + s))
    src = os.path.join(d, 'R.java')
    suffix = getPkgSuffix(channel)
    if len(suffix) == 0:
        return
    newPkgName = pkgName + '.' + suffix
    targetD = os.path.join(d, suffix)
    target = os.path.join(targetD, 'R.java')
    if not os.path.exists(src):
        error('Fail to locate old source %s' %src)
    if not os.path.exists(targetD):
        os.makedirs(targetD)
    if not os.path.exists(target) or isNewerThan(src, target):
        with codecs.open(src, 'r', 'utf8') as srcF, codecs.open(target, 'w', 'utf8') as targetF:
            for l in srcF.readlines():
                if l.startswith('package %s;' %pkgName):
                    targetF.write('package %s;\n' %newPkgName)
                else:
                    targetF.write(l)

def getPkgSuffix(channel):
    doc = xml.parse(os.path.join('chameleon', 'channels', channel, 'info.xml'))
    t = doc.documentElement.getAttribute('pkgsuffix')
    if len(t) > 1:
        return t[1:]
    else:
        return '' 

def main():
    if len(sys.argv) < 4:
        return -1
    channel = sys.argv[1]
    manifestFilePath = sys.argv[2]
    genPath = sys.argv[3]
    pkgName = sys.argv[4]
    error(pkgName)
    globalcfg = getCommCfg()
    libs = getDependLibs(globalcfg, channel)   
    modifyManifest(channel, libs, manifestFilePath) 
    genInstantializer(channel, genPath, globalcfg, libs)

sys.exit(main())
