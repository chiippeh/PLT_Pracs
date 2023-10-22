@echo off
if "%1" == "" goto missing
echo Formatting %1.pav to %1.ppv
java FormatParva.FormatParva %1.pav -l %2 %3 %4 %5
goto stop
:missing
echo no Parva program specified - use FormatPav ParvaFile [options] (omit .pav extension)
goto end
:stop
echo Now try compiling Parva %1.ppv
:end

