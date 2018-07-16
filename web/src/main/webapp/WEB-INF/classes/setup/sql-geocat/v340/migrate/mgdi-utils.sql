-- helpers used in MGDI migration

-- find all duplicate extents
select * from (
select uuid, isTemplate, root,
  (
    select string_agg(uuid, '
')
      from metadata t2
      where regexp_replace(t1.data, 'gml:id=".*"', '', 'g') =
        regexp_replace(t2.data, 'gml:id=".*"', '', 'g')
        and t2.isTemplate = 's'
        and t2.uuid like 'geocatch-%'
  ) as dupes,
  (
    select count(*) from metadata t3
      where regexp_replace(t1.data, 'gml:id=".*"', '', 'g') =
        regexp_replace(t3.data, 'gml:id=".*"', '', 'g')
        and t3.isTemplate = 's'
        and t3.uuid like 'geocatch-%'
  ) as dupes_count
  from metadata t1
) t where t.uuid = split_part(t.dupes, '
', 1) and t.uuid like 'geocatch-%' and dupes_count > 1;

-- find extents with multiple polygons
select uuid, label2, label3, string_agg(cast(matches as text), '
'), count(matches) as c from (
    SELECT uuid,
        xpath(
            './/gmd:description/gmd:PT_FreeText/*[2]/*/text()',
            data::xml,
            ARRAY[ARRAY['gmd', 'http://www.isotc211.org/2005/gmd'],
                ARRAY['gco', 'http://www.isotc211.org/2005/gco']]
        )::varchar[] AS label2,
        xpath(
            './/gmd:description/gmd:PT_FreeText/*[3]/*/text()',
            data::xml,
            ARRAY[ARRAY['gmd', 'http://www.isotc211.org/2005/gmd'],
                ARRAY['gco', 'http://www.isotc211.org/2005/gco']]
        )::varchar[] AS label3,
        regexp_matches(data, 'Polygon gml:id="[^"]*">', 'g') as matches
    FROM metadata WHERE istemplate = 's' and uuid like 'geocatch-subtpl-extent-%'
) as s group by uuid, label2, label3 order by c desc;

-- raw list of duplicate extents (all non validated)
-- 2
-- 3
-- 4
-- 5
-- 6
-- 7
-- 8
-- 10
-- 18
--
-- 22
-- 41
-- 43
-- 44
-- 45
-- 46
-- 47
-- 48
-- 53
-- 54
-- 56
-- 57
-- 61
-- 63
-- 64
-- 66
-- 67
-- 69
-- 71
-- 72
-- 73
-- 74
-- 75
-- 77
-- 79
-- 80
-- 81
-- 82
-- 84
-- 85
-- 87
-- 88
-- 89
-- 91
-- 93
-- 94
-- 96
-- 98
-- 103
-- 105
-- 106
-- 110
-- 112
-- 115
--
-- 49
-- 55
-- 58
-- 70
-- 101
-- 108
-- 113
--
-- 42
-- 62
-- 76
-- 78
-- 83
-- 90
-- 92
-- 95
-- 99
