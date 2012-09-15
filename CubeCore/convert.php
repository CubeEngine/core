#!/usr/bin/php
<?php
    define('SOURCE', 'items.csv');
    define('TARGET', 'items.txt');
    define('SKIPPED', 'skippeditems.txt');

    if (!is_readable(SOURCE))
    {
        echo SOURCE . " not found!\n";
        exit(1);
    }

    $target = @fopen(TARGET, 'wb');
    if (!$target)
    {
        echo "Failed to open " . TARGET . " for writing!\n";
        exit(1);
    }
    
    $skipped = @fopen(SKIPPED, 'wb');
    if (!$target)
    {
        echo "Failed to open " . SKIPPED . " for writing!\n";
        exit(1);
    }

    $lines = file(SOURCE);
    $lastId = -1;
    $lastData = -1;
    $lastName = null;
    $i = 0;
    foreach ($lines as $line)
    {
        ++$i;
        $line = trim($line);
        if (empty($line) || $line[0] == '#' || substr_count($line, ',') !== 2)
        {
            continue;
        }
        list($name, $id, $data) = explode(',', $line);
        
        if (!is_null($lastName) && levenshtein($lastName, $name) <= 3)
        {
            fwrite($skipped, "$name\n");
            continue;
        }
        $lastName = $name;
        
        if ($lastId == $id && $lastData == $data )
        {
            fwrite($target, "  $name\n");
        }
        else
        {
            fwrite($target, "$id:$data\n  $name\n");
            $lastId = $id;
            $lastData = $data;
            $lastName = null;
        }
    }
    fclose($target);
    fclose($skipped);
?>