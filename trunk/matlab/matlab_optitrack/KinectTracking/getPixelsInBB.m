function frame = getPixelsInBB(img, BBox)

if length(size(img)) == 2 % depth image
    frame = img(max([BBox(2),1]):min([BBox(2) + BBox(4),480]), ...
        max([BBox(1),1]):min([BBox(1) + BBox(3), 640]));
elseif length(size(img)) == 3 % RGB image
    frame = img(max([BBox(2),1]):min([BBox(2) + BBox(4),480]), ...
        max([BBox(1),1]):min([BBox(1) + BBox(3), 640]),:);
end

