filepath = 'D:\GitProjects\jimzMLConverter\jimzMLConverter\src\test\resources\';

filename = [filepath 'test.grd'];

fid = fopen(filename, 'w');

writeIon = @(fid, scan, shot, x, y, tof) fwrite(fid, [scan shot x y tof], 'uint32');

writeIon(fid, 1, 1, 0, 0, 1e5);
writeIon(fid, 2, 1, 0, 0, 2e5);
writeIon(fid, 3, 2, 0, 0, 1e5);
writeIon(fid, 4, 3, 0, 0, 1e5);
writeIon(fid, 5, 4, 1, 1, 1e5);
writeIon(fid, 6, 4, 1, 1, 3e5);


fclose(fid);


%%

filename = ['D:\GitProjects\jimzMLConverter\jimzMLConverter\target\test-classes\test.grd.ibdtmp'];

fid = fopen(filename);
data = fread(fid, Inf, 'uint32', 0, 'b')
fclose(fid);