command = 'java -Djava.library.path=lib/ -jar jimzMLConverter-1.0-SNAPSHOT.jar hdf5 -o comp%d_%d_%d_%d.h5 --compression-level %d --chunk %d %d %d "D:\\GitProjects\\jimzMLConverter\\jimzMLConverter\\target\\test-classes\\IM_500_IM_S.raw.imzML"';

levels = 1:2:9;

chunk1 = 10;
chunk2s = 500:500:5000;
chunk3 = 200;

index = 1;
results = [];

for level = levels
    for chunk2 = chunk2s

        results(index).execCommand = sprintf(command, level, chunk1, chunk2, chunk3, level, chunk1, chunk2, chunk3);
%    results(index).execCommand
        tic;
         system(results(index).execCommand);
        results(index).timeTaken = toc;
    end
end

