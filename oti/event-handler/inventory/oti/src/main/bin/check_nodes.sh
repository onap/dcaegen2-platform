echo "zgrep \"VNFC;\" $1 | wc -l"
zgrep "VNFC;" $1 | wc -l

echo " \"VNFC;PROV\" $1 | wc -l"
zgrep "VNFC;PROV" $1 | wc -l

echo " \"VNFC;NVTPROV;\" $1 | wc -l"
zgrep "VNFC;NVTPROV;" $1 | wc -l

echo " \"VNFC;NA;\" $1 | wc -l"
zgrep "VNFC;NA;" $1 | wc -l

echo " \"VNFC;MAINT;\" $1 | wc -l"
zgrep "VNFC;MAINT;" $1 | wc -l

echo " \"VNFC;PREPROV;\" $1 | wc -l"
zgrep "VNFC;PREPROV;" $1 | wc -l

echo " \"VM;\" $1 | wc -l"
zgrep "VM;" $1 | wc -l

echo " \"VM;PROV\" $1 | wc -l"
zgrep "VM;PROV" $1 | wc -l

echo " \"VM;NVTPROV;\" $1 | wc -l"
zgrep "VM;NVTPROV;" $1 | wc -l

echo " \"VM;NA;\" $1 | wc -l"
zgrep "VM;NA;" $1 | wc -l

echo " \"VM;MAINT;\" $1 | wc -l"
zgrep "VM;MAINT;" $1 | wc -l

echo " \"VM;PREPROV;\" $1 | wc -l"
zgrep "VM;PREPROV;" $1 | wc -l

echo " \"VNF;\" $1 | wc -l"
zgrep "VNF;" $1 | wc -l

echo " \"VNF;PROV\" $1 | wc -l"
zgrep "VNF;PROV" $1 | wc -l

echo " \"VNF;NVTPROV;\" $1 | wc -l"
zgrep "VNF;NVTPROV;" $1 | wc -l

echo " \"VNF;NA;\" $1 | wc -l"
zgrep "VNF;NA;" $1 | wc -l

echo " \"VNF;MAINT;\" $1 | wc -l"
zgrep "VNF;MAINT;" $1 | wc -l

echo " \"VNF;PREPROV;\" $1 | wc -l"
zgrep "VNF;PREPROV;" $1 | wc -l