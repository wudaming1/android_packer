import sys
import getopt
import shutil
import zipfile
import os
import struct

__author__ = 'Administrator'
# ZIP_SHORT和MAGIC常量需要与app中的相应常量一致
ZIP_SHORT = 2
MAGIC = '!ZXK!'
# 如果没有输入,则使用如下默认值
input_file = os.path.abspath(os.curdir)+"/app/build/outputs/apk/app-debug.apk"
output_file = os.path.abspath(os.curdir)+'/out'
prop_file = os.path.abspath(os.curdir)+"app/src/main/assets/channel-maps.properties"

def write_market(apk_file, market):
    print('start to run market packaging testing...')
    index = os.stat(apk_file).st_size
    index -= ZIP_SHORT
    with open(apk_file, "r+b") as f:
        f.seek(index)
        # write comment length
        f.write(struct.pack('<H', len(market) + ZIP_SHORT + len(MAGIC)))
        # write comment content
        # content = [market_string + market_length + magic_string]
        f.write(market)
        f.write(struct.pack('<H', len(market)))
        f.write(MAGIC)
    return apk_file

def read_market(path):
    '''
    read market info from apk file
    read_market(apk-file-path)
    '''
    index = os.stat(path).st_size
    # print('path:',path,'length:',index)
    index -= len(MAGIC)
    f = open(path, 'rb')
    f.seek(index)
    # read and check magic
    magic = f.read(len(MAGIC))
    # print('magic',magic)
    if magic == MAGIC:
        index -= ZIP_SHORT
        f.seek(index)
        # read market string length
        market_length = struct.unpack('<H', f.read(ZIP_SHORT))[0]
        # print('comment length:',market_length)
        index -= market_length
        f.seek(index)
        # read market
        market = f.read(market_length)
        # print('found market:',market)
        return market
    else:
        # print('magic not matched')
        return None



opts, args = getopt.getopt(sys.argv[1:], "hi:o:p:")
for op, value in opts:

    if op == "-i":
        input_file = value
    elif op == "-o":
        output_file = value
    elif op == "-p":
        prop_file = value


pre_file = open(prop_file, "r")
strs = pre_file.readlines()
pre_file.close()

for line in strs:
    pcodeStartid = line.find(':', 0, line.__len__())
    pcodeEndid = line.find('#', pcodeStartid, line.__len__())
    pcode = line[pcodeStartid + 1:pcodeEndid]
    Channel = line[pcodeStartid + 1:].replace('#', '_').replace('\n', '')
    if len(Channel.split('_')) == 3:
        print('umeng')
        apkname = output_file + '/umeng/Letvclient' + pcode + '.apk'
        if not os.path.exists(output_file+'/umeng'):
            os.mkdir(output_file+'/umeng')
        shutil.copy(input_file, apkname)
        write_market(apkname, Channel)
    else:
        apkname = output_file + '/Letvclient' + pcode + '.apk'
        shutil.copy(input_file, apkname)
        write_market(apkname, Channel)

    print (apkname, Channel)

